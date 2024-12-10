package com.pcdd.sonovel;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Example
 * This Java class demonstrates how to execute commands and handle the output in a command-line interface.
 *
 * Usage
 * Compile the Java class.
 * Run the compiled class.
 * $ javac Example.java
 * $ java Example
 * Enter commands in the command-line interface (CLI).
 * Description
 * The Example class provides a CLI interface where you can enter commands to be executed. The Terminal and LineReader classes from the JLine library are used to create the CLI interface.
 *
 * The main method sets up the CLI environment and enters a loop to read user input. Each line of input is passed to the exec method, which executes the command using ProcessBuilder.
 *
 * The exec method starts a new process to execute the command and redirects the standard error to the standard output. It then creates two separate threads to handle the standard output and standard error streams respectively. These threads read the output line by line and print it to the console. After that, it waits for the command to complete and prints the exit code. Finally, it waits for the output and error threads to finish.
 *
 * Examples
 *
 * > echo Hello, world!
 * > input: Hello, world!
 * > 命令执行结束，退出码: 0
 * > ls
 * > input: file1.txt
 * > input: file2.txt
 * > 命令执行结束，退出码: 0
 * > invalid-command
 * > error: 'invalid-command' is not recognized as an internal or external command,
 * > operable program or batch file.
 * > 命令执行结束，退出码: 1
 * > exit
 * > In the example above, the user enters various commands and the output is displayed accordingly. The exit code indicates whether the command executed successfully or not.
 *
 * Feel free to modify and expand upon this example to suit your needs.
 *
 * Dependencies
 * JLine library is used for the CLI interface. Make sure to include the JLine library in your project.
 */
public class Example {

    public static void main(String[] args) throws IOException {
        Terminal terminal = TerminalBuilder.builder().build();
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        String line;
        while ((line = lineReader.readLine("> ")) != null) {
            // 在此处处理命令行输入的line
            exec(line);
        }
    }

    /**
     * 执行命令
     */
    static void exec(String cmd) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", cmd);
            processBuilder.redirectErrorStream(true); // 将错误输出合并到标准输出

            Process process = processBuilder.start();

            // 处理标准输出
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("input: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 处理标准错误
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println("error: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 启动线程处理输出和错误
            outputThread.start();
            errorThread.start();

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("命令执行结束，退出码: " + exitCode);

            // 等待线程执行完成
            outputThread.join();
            errorThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
