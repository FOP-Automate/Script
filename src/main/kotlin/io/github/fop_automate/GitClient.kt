package io.github.fop_automate

import java.io.File
import javax.management.loading.ClassLoaderRepository

/**
 * A class to interact with a git repository
 * @param originUrl the url of the remote repository
 * @param repository the local repository
 */
@Suppress("MemberVisibilityCanBePrivate")
class GitClient(
    var originUrl: String,
    repository: File,
) {

    val repository = repository.absoluteFile

    /**
     * Set the remote of the repository
     * @param name the name of the remote
     * @param url the url of the remote
     */
    fun setRemote(name: String, url: String) {
        // If update origin
        if (hasRemote(name)) {
            val remoteUrl = getRemoteUrl(name)
            if (remoteUrl != url) {
                executeGitCommand("remote", "remove", name)
                executeGitCommand("remote", "add", name, url)
            }
        } else executeCommand("git", "remote", "add", name, url)
    }

    /**
     * Check if the repository has a remote with the given name
     * @param name the name of the remote
     * @return true if the repository has a remote with the given name
     */
    fun hasRemote(name: String): Boolean {
        return executeGitCommand("remote").contains(name)
    }

    /**
     * Get the remote url of the repository
     * @param name the name of the remote
     * @return the url of the remote
     */
    fun getRemoteUrl(name: String): String? {
        return executeGitCommand("remote", "get-url", name).ifBlank { null }.stripNewLines()
    }

    /**
     * Get a file from the repository
     * @param path the path of the file
     * @return the file
     */
    fun file(path: String): File {
        return File(repository, path)
    }

    /**
     * Get the content of a file from the repository
     * @param path the path of the file
     * @return the content of the file
     */
    fun fileContent(path: String): String {
        return file(path).readText()
    }

    /**
     * Set the content of a file from the repository
     * @param path the path of the file
     * @param content the content of the file
     */
    fun setFileContent(path: String, content: String) {
        file(path).writeText(content)
    }

    /**
     * Add a file to the repository
     * @param path the path of the file
     */
    fun addFile(path: String) {
        executeGitCommand("add", "--", path)
    }

    /**
     * Add a file to the repository
     * @param file the file
     */
    fun addFile(file: File) {
        val path = file.relativeTo(repository).path
        if (path.contains("..")) throw IllegalArgumentException("File is not in the repository")
        addFile(path)
    }

    /**
     * Add files to the repository
     * @param paths the paths of the files
     */
    fun addFiles(vararg paths: String) {
        executeGitCommand("add", "--", *paths)
    }

    /**
     * Add files to the repository
     * @param files the files
     */
    fun addFiles(vararg files: File) {
        val paths = files.map {
            val path = it.relativeTo(repository).path
            if (path.contains("..")) throw IllegalArgumentException("File is not in the repository")
            path
        }.toTypedArray()
        addFiles(*paths)
    }

    /**
     * Commit the changes of the repository
     * @param message the message of the commit
     */
    fun commit(message: String) {
        executeGitCommand("commit", "-m", message)
    }

    fun commit(message: String, vararg paths: String): Boolean {
        if (paths.isEmpty()) return false

        // Check if the files are changed
        if (paths.none { changed(it) }) return false

        // Add the files
        addFiles(*paths)

        // Commit the changes
        executeGitCommand("commit", "-m", message, *paths)
        return true
    }

    fun getMainBranch(): String {
        return executeGitCommand("branch", "--show-current").stripNewLines()
    }

    fun getOriginMainBranch(): String {
        return (executeGitCommand("remote", "show", "origin", "-n")
            .lines()
            .find { it.startsWith("HEAD branch") }?.split(":")?.get(1)?.trim() ?: "main").stripNewLines()
    }

    /**
     * Push the changes of the repository
     * @param mirror if the push is a mirror
     */
    fun push(
        mirror: Boolean = false,
        remote: String = "origin",
        branch: String = getMainBranch(),
        originBranch: String = getOriginMainBranch()
    ) {
        val args = mutableListOf("push")
        if (mirror) args.add("--mirror")
        if(!mirror) args.add(remote)
        if(!mirror) args.add("${branch}:${originBranch}")
        executeGitCommand(*args.toTypedArray())
    }

    /**
     * Pull the changes of the repository
     */
    fun clone(url: String = originUrl, directory: File = repository.parentFile) {
        directory.parentFile.mkdirs()
        executeCommand("git", "clone", url, directory.path, cwd = directory.parentFile.path)
    }

    /**
     * Ensure the repository is cloned
     * @param url the url of the remote repository
     * @param directory the local repository
     */
    fun ensureClone(url: String = originUrl, directory: File = repository) {
        if (!directory.exists()) clone(url, directory)
    }

    /**
     * Initialize the repository
     */
    fun init() {
        repository.mkdirs()
        executeGitCommand("init")
    }

    /**
     * Ensure the repository is initialized
     * @return true if the repository was initialized
     */
    fun ensureInit(): Boolean {
        if(!File(repository, ".git").exists()){
            init()
            return true
        }
        return false
    }

    /**
     * Get the branch of the repository
     * @return the branch of the repository
     */
    fun changedFiles(): List<String> {
        return executeGitCommand("diff", "--name-only", "HEAD").lines()
    }

    /**
     * Check if a file is changed
     * @param path the path of the file
     * @return true if the file is changed
     */
    fun changed(path: String): Boolean {
        return diff(path) || lsFiles(path)
    }

    private fun diff(path: String): Boolean {
        return executeGitCommand("diff", "--", path).isNotBlank()
    }

    private fun lsFiles(path: String): Boolean {
        return try {
            executeGitCommand("ls-files", "--error-unmatch", "--", path).isBlank()
        } catch (e: RuntimeException) {
            true
        }
    }

    /**
     * Check if a file is changed
     * @param file the file
     * @return true if the file is changed
     */
    fun changed(file: File): Boolean {
        return changed(file.relativeTo(repository).path)
    }

    /**
     * Check if the repository is clean
     * @return true if the repository is clean
     */
    fun originRepositoryExists(): Boolean {
        try {
            return executeGHCommand("repo", "view", originUrl).isNotBlank()
        } catch (e: RuntimeException) {
            return false
        }
    }

    /**
     * Create the origin repository
     * @param private if the repository is private
     */
    fun createOriginRepository(private: Boolean = true) {
        executeGHCommand("repo", "create", originUrl, "--private=${private}")
        push(mirror = true)
    }

    /**
     * Delete the origin repository
     */
    fun deleteOriginRepository() {
        executeGHCommand("repo", "delete", originUrl, "--yes")
    }

    /**
     * Ensure the origin repository exists
     * @param private if the repository is private
     * @return true if the origin repository was created
     */
    fun ensureOriginRepository(private: Boolean = true): Boolean {
        if (!originRepositoryExists()) {
            createOriginRepository(private)
            return true
        }
        return false
    }

    /**
     * Check if the repository exists
     * @return true if the repository exists
     */
    fun exists() = repository.exists()

    /**
     * Create the repository
     */
    fun mkdirs(path: String) =
        File(repository, path).mkdirs()

    fun mkdirs() = repository.mkdirs()

    /**
     * Copy a resource to the repository
     */
    fun copyResource(resource: String, path: String) {
        val resourceStream =
            javaClass.getResourceAsStream(resource) ?: throw IllegalArgumentException("Resource not found: $resource")
        val file = File(repository, path)
        file.parentFile.mkdirs()
        file.outputStream().use {
            resourceStream.copyTo(it)
            resourceStream.close()
        }
    }

    /**
     * Execute a git command
     * @param args the arguments of the command
     * @return the output of the command
     */
    fun executeGitCommand(vararg args: String): String {
        return executeCommand("git", *args)
    }

    /**
     * Execute a gh command
     * @param args the arguments of the command
     * @return the output of the command
     */
    fun executeGHCommand(vararg args: String): String {
        return executeCommand("gh", *args)
    }

    /**
     * Execute a command
     * @param command the command to execute
     * @param args the arguments of the command
     * @return the output of the command
     */
    fun executeCommand(command: String, vararg args: String): String {
        return executeCommand(command, *args, cwd = repository.path)
    }

}

fun String.stripNewLines() = replace(Regex("[\n\r]"), "")

@JvmName("stripNewLinesNullable")
fun String?.stripNewLines() = this?.stripNewLines()