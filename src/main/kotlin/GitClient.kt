package org.example

import java.io.File

/**
 * A class to interact with a git repository
 * @param originUrl the url of the remote repository
 * @param repository the local repository
 */
@Suppress("MemberVisibilityCanBePrivate")
class GitClient(
    private val originUrl: String,
    private val repository: File,
) {

    /**
     * Set the remote of the repository
     * @param name the name of the remote
     * @param url the url of the remote
     */
    fun setRemote(name: String, url: String) {
        // If update origin
        if(hasRemote(name)) {
            val remoteUrl = getRemoteUrl(name)
            if(remoteUrl != url) {
                executeGitCommand("remote", "remove", name)
                executeGitCommand("remote", "add", name, url)
            }
        }

        else executeCommand("git", "remote", "add", name, url)
    }

    /**
     * Check if the repository has a remote with the given name
     * @param name the name of the remote
     * @return true if the repository has a remote with the given name
     */
    fun hasRemote(name: String): Boolean {
        return executeGitCommand( "remote").contains(name)
    }

    /**
     * Get the remote url of the repository
     * @param name the name of the remote
     * @return the url of the remote
     */
    fun getRemoteUrl(name: String): String? {
        return executeGitCommand( "remote", "get-url", name).ifBlank { null }
    }

    /**
     * Execute a git command
     * @param args the arguments of the command
     * @return the output of the command
     */
    fun executeGitCommand(vararg args: String) : String {
        return executeCommand("git", *args)
    }

    /**
     * Execute a gh command
     * @param args the arguments of the command
     * @return the output of the command
     */
    fun executeGHCommand(vararg args: String) : String {
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
        executeGitCommand("add", path)
    }

    /**
     * Add a file to the repository
     * @param file the file
     */
    fun addFile(file: File) {
        val path = file.relativeTo(repository).path
        if(path.contains("..")) throw IllegalArgumentException("File is not in the repository")
        addFile(path)
    }

    /**
     * Add files to the repository
     * @param paths the paths of the files
     */
    fun addFiles(vararg paths: String) {
        executeGitCommand("add", *paths)
    }

    /**
     * Add files to the repository
     * @param files the files
     */
    fun addFiles(vararg files: File) {
        val paths = files.map {
            val path = it.relativeTo(repository).path
            if(path.contains("..")) throw IllegalArgumentException("File is not in the repository")
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

    /**
     * Push the changes of the repository
     * @param mirror if the push is a mirror
     */
    fun push(mirror: Boolean = false) {
        val args = mutableListOf("push")
        if(mirror) args.add("--mirror")
        executeGitCommand(*args.toTypedArray())
    }

    /**
     * Pull the changes of the repository
     */
    fun clone(url: String = originUrl, directory: File = repository.parentFile) {
        directory.parentFile.mkdirs()
        executeGitCommand("clone", url, directory.path)
    }

    fun ensureClone(url: String = originUrl, directory: File = repository.parentFile) {
        if(!directory.exists()) clone(url, directory)
    }

    fun changedFiles(): List<String> {
        return executeGitCommand("diff", "--name-only", "HEAD").lines()
    }

    fun changed(path: String): Boolean {
        return executeGitCommand("diff", "--name-only", "HEAD", path).isNotBlank()
    }

    fun changed(file: File): Boolean {
        return changed(file.relativeTo(repository).path)
    }

    fun originRepositoryExists(): Boolean {
        return executeGHCommand("repo", "view", originUrl).isNotBlank()
    }

    fun createOriginRepository(private: Boolean = true) {
        executeGHCommand("repo", "create", originUrl, "--private=${private}")
        push(mirror = true)
    }

    fun deleteOriginRepository() {
        executeGHCommand("repo", "delete", originUrl, "--yes")
    }

    fun ensureOriginRepository(private: Boolean = true): Boolean {
        if(!originRepositoryExists()) {
            createOriginRepository(private)
            return true
        }
        return false
    }

    fun exists() = repository.exists()

    fun mkdirs(path: String) =
        File(repository, path).mkdirs()

    fun copyResource(resource: String, path: String) {
        val resourceStream =
            javaClass.getResourceAsStream(resource) ?: throw IllegalArgumentException("Resource not found")
        val file = File(repository, path)
        file.parentFile.mkdirs()
        file.outputStream().use { resourceStream.copyTo(it) }
    }


}