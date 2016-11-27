/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fileSystem;

import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author deneir-uy
 */
class Node implements Serializable {

    Node parent;
    HashMap<String, Node> children;
    HashMap<String, Node> files;
    Descriptor info;
    String key;

    Node(String address, Node parent, Date date) {
        this.parent = parent;
        this.children = new HashMap<>();
        this.files = new HashMap<>();
        this.info = new Descriptor(address, date);
        this.key = address;
    }

    Node(String filename, Node parent, String contents, Date date) {
        this.parent = parent;
        this.info = new Descriptor(filename, contents, date);

    }

    public void listContents(String args) {
        int space = getLongestName() + 3;
        String format = "%-" + Integer.toString(space) + "s%-9s%-22s%s%n";
        String pattern = "hh:mma MMM/dd/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        children.entrySet().stream().map((entry) -> (Node) children.get(entry.getKey())).forEach((child) -> {
            if (Pattern.matches(args.replace("*", ".*"), child.info.getFilename())) {
                System.out.printf(format, child.info.getFilename(), "Folder",
                        dateFormat.format(child.info.getDateCreated()),
                        dateFormat.format(child.info.getDateLastModified()));
            }
        });

        files.entrySet().stream().map((entry) -> (Node) files.get(entry.getKey())).forEach((file) -> {
            if (Pattern.matches(args.replace("*", ".*"), file.info.getFilename())) {
                System.out.printf(format, file.info.getFilename(), "File",
                        dateFormat.format(file.info.getDateCreated()),
                        dateFormat.format(file.info.getDateLastModified()));
            }
        });
    }

    private int getLongestName() {
        int longest = 0;

        Iterator iChildren = children.entrySet().iterator();
        Iterator iFiles = files.entrySet().iterator();
        
        while (iChildren.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) iChildren.next();
            if (pair.getKey().toString().length() > longest) {
                longest = pair.getKey().toString().length();
            }
        }
        
        while (iFiles.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) iFiles.next();
            if (pair.getKey().toString().length() > longest) {
                longest = pair.getKey().toString().length();
            }
        }

        return longest;
    }

    public void updateDates() {
        Date date = new Date();

        children.entrySet().stream().map((entry) -> (Node) children.get(entry.getKey())).forEach((child) -> {
            child.info.setDateCreated(date);
            child.info.setDateLastModified(date);
        });

        files.entrySet().stream().map((entry) -> (Node) files.get(entry.getKey())).forEach((file) -> {
            file.info.setDateCreated(date);
            file.info.setDateLastModified(date);
        });
    }
}

class Descriptor implements Serializable {

    private String filename;
    private String contents;
    private Date dateCreated;
    private Date dateLastModified;
    boolean isDirectory;

    Descriptor(String filename, String contents, Date date) {
        this.filename = filename;
        this.contents = contents;
        this.dateCreated = date;
        this.dateLastModified = date;
        this.isDirectory = false;
    }

    Descriptor(String filename, Date date) {
        this.filename = filename;
        this.contents = "";
        this.dateCreated = date;
        this.dateLastModified = date;
        this.isDirectory = true;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(Date dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

}

class Tree implements Serializable {

    Node root, currentNode;

    Tree(Node root) {
        this.root = root;
        this.currentNode = this.root;
    }

    public String getPath(String path, Node currNode) {
        if (currNode.parent == null) {
            return path;
        } else {
            path = currNode.parent.key + "/" + path;
            return getPath(path.trim(), currNode.parent);
        }
    }

    public void makeDirectory(String[] key, Date date) {
        String name = key[key.length - 1].substring(key[key.length - 1].lastIndexOf(">") + 1).trim();
        if (key.length == 1) {
            Node newNode = new Node(key[0].trim(), currentNode, date);
            currentNode.children.put(key[0].trim(), newNode);
        } else {

            for (int i = 0; i < key.length - 1; i++) {
                if (!key[i].equals("")) {
                    currentNode = changeDirectory(key[i].trim());
                }
            }

            if (currentNode == null) {
                return;
            }

            Node newNode = new Node(name, currentNode, date);
            currentNode.children.put(name, newNode);
        }
    }

    public void removeDirectory(String[] key) {
        String filename = key[key.length - 1].trim();

        if (key.length == 1) {
            currentNode.children.remove(key[0].trim());
        } else {

            for (int i = 0; i < key.length - 1; i++) {
                if (!key[i].equals("")) {
                    currentNode = changeDirectory(key[i].trim());
                }
            }

            if (currentNode == null) {
                return;
            }

            if (currentNode.children.containsKey(filename)) {
                currentNode.children.remove(filename);
            }
        }
    }

    public void makeFile(String[] key, String contents, Date date) {
        String name = key[key.length - 1].substring(key[key.length - 1].lastIndexOf(">") + 1).trim();

        if (key.length == 1) {
            Node newNode = new Node(name, currentNode, contents, date);
            currentNode.files.put(name, newNode);
        } else {
            for (int i = 0; i < key.length - 1; i++) {
                if (!key[i].equals("")) {
                    currentNode = changeDirectory(key[i].trim());
                }
            }

            if (currentNode == null) {
                return;
            }

            Node newNode = new Node(name, currentNode, date);
            currentNode.files.put(name, newNode);
        }
    }

    public void removeFile(String[] key) {
        String name = key[key.length - 1].substring(key[key.length - 1].lastIndexOf(">") + 1).trim();

        if (key.length == 1) {
            currentNode.files.remove(key[0].trim());
        } else {
            for (int i = 0; i < key.length - 1; i++) {
                if (!key[i].equals("")) {
                    currentNode = changeDirectory(key[i].trim());
                }
            }

            if (currentNode == null) {
                return;
            }

            if (currentNode.files.containsKey(name)) {
                currentNode.files.remove(name);
            }
        }
    }

    public void updateFile(String[] key, String contents, Date date) {
        String name = key[key.length - 1].substring(key[key.length - 1].lastIndexOf(">") + 1).trim();

        if (key.length == 1) {
            currentNode.files.get(name).info.setContents(contents);
            currentNode.files.get(name).info.setDateLastModified(date);

        } else {
            for (int i = 0; i < key.length - 1; i++) {
                if (!key[i].equals("")) {
                    currentNode = changeDirectory(key[i].trim());
                }
            }

            if (currentNode == null) {
                return;
            }

            currentNode.files.get(name).info.setContents(contents);
            currentNode.files.get(name).info.setDateLastModified(date);
        }
    }

    public void moveDirectory(String filename, String[] address, Node preNode) {
        Node moveNode = preNode.children.get(filename);

        if (address.length == 1) {
            if (preNode.children.containsKey(address[0])) {
                preNode.children.get(filename).parent = preNode.children.get(address[0]);
                preNode.children.remove(filename);
                preNode.children.get(address[0]).children.put(filename, moveNode);
            } else if (address[0].equals("root")) {
                preNode.children.get(filename).parent = root;
                preNode.children.remove(filename);
                root.children.put(filename, moveNode);
            } else if (address[0].equals("..")) {
                preNode.children.get(filename).parent = preNode.parent;
                preNode.children.remove(filename);
                preNode.parent.children.put(filename, moveNode);
            } else {
                System.out.println(address[0] + ": no such directory found");
            }
        } else {

            for (String addres : address) {
                if (!addres.equals("")) {
                    currentNode = changeDirectory(addres.trim());
                }
            }

            if (currentNode == null) {
                return;
            }

            moveNode.parent.children.remove(filename);
            moveNode.parent = currentNode;
            currentNode.children.put(filename, moveNode);

        }
    }

    public void moveFile(String filename, String[] address, Node preNode) {
        Node moveNode = preNode.files.get(filename);
        String newFilename = filename;

        if (address.length == 1) {
            if (preNode.children.containsKey(address[0])) {
                preNode.files.get(filename).parent = preNode.children.get(address[0]);
                preNode.files.remove(filename);
                preNode.children.get(address[0]).files.put(filename, moveNode);
            } else if (address[0].equals("root")) {
                preNode.files.get(filename).parent = root;
                preNode.files.remove(filename);
                root.files.put(filename, moveNode);
            } else if (address[0].equals("..")) {
                preNode.files.get(filename).parent = preNode.parent;
                preNode.files.remove(filename);
                preNode.parent.files.put(filename, moveNode);
            } else {
                System.out.println(address[0] + ": no such directory found");
            }
        } else {

            for (int i = 0; i < address.length; i++) {
                if (!address[i].equals("")) {
                    currentNode = checkIfDirectory(address[i].trim());
                }
            }

            if (!currentNode.key.equals(address[address.length - 1])) {
                newFilename = address[address.length - 1];
                moveNode.info.setFilename(newFilename);
                moveNode.key = newFilename;
            }

            moveNode.parent.files.remove(filename);
            moveNode.parent = currentNode;
            currentNode.files.put(newFilename, moveNode);

        }
    }

    public void copyDirectory(String filename, String[] address, Node preNode) {
        Date date = new Date();
        Node copyNode = new Node(filename, preNode, date);

        if (address.length == 1) {
            if (preNode.children.containsKey(address[0])) {
                preNode.children.get(filename).parent = preNode.children.get(address[0]);
                copyNode.updateDates();
                preNode.children.get(address[0]).children.put(filename, copyNode);
            } else if (address[0].equals("root")) {
                preNode.children.get(filename).parent = root;
                copyNode.updateDates();
                root.children.put(filename, copyNode);
            } else if (address[0].equals("..")) {
                preNode.children.get(filename).parent = preNode.parent;
                copyNode.updateDates();
                preNode.parent.children.put(filename, copyNode);
            } else {
                System.out.println(address[0] + ": no such directory found");
            }
        } else {

            for (String addres : address) {
                if (!addres.equals("")) {
                    currentNode = changeDirectory(addres.trim());
                }
            }

            if (currentNode == null) {
                return;
            }

            copyNode.updateDates();
            copyNode.parent = currentNode;
            currentNode.children.put(filename, copyNode);

        }
    }

    public void copyFile(String filename, String[] address, Node preNode) {
        Date date = new Date();
        Node copyNode = new Node(filename, preNode,
                preNode.files.get(filename).info.getContents(), date);
        String newFilename = filename;

        if (address.length == 1) {
            if (preNode.children.containsKey(address[0])) {
                preNode.files.get(filename).parent = preNode.children.get(address[0]);
                preNode.children.get(address[0]).files.put(filename, copyNode);
            } else if (address[0].equals("root")) {
                preNode.files.get(filename).parent = root;
                root.files.put(filename, copyNode);
            } else if (address[0].equals("..")) {
                preNode.files.get(filename).parent = preNode.parent;
                preNode.parent.files.put(filename, copyNode);
            } else {
                copyNode.info.setFilename(address[0]);
                copyNode.key = address[0];
                preNode.files.put(address[0], copyNode);
            }
        } else {

            for (int i = 0; i < address.length; i++) {
                if (!address[i].equals("")) {
                    currentNode = checkIfDirectory(address[i].trim());
                }
            }

            if (!currentNode.key.equals(address[address.length - 1])) {
                newFilename = address[address.length - 1];
                copyNode.info.setFilename(newFilename);
                copyNode.key = newFilename;
            }

            copyNode.parent = currentNode;
            currentNode.files.put(newFilename, copyNode);

        }
    }

    public void renameDirectory(String filename, String newFilename) {
        Node renamedNode = currentNode.children.get(filename);

        currentNode.children.remove(filename);
        renamedNode.info.setFilename(newFilename);
        renamedNode.key = newFilename;
        currentNode.children.put(newFilename, renamedNode);
    }

    public void renameFile(String filename, String newFilename) {
        Node renamedNode = currentNode.files.get(filename);

        currentNode.files.remove(filename);
        renamedNode.info.setFilename(newFilename);
        renamedNode.key = newFilename;
        currentNode.files.put(newFilename, renamedNode);
    }

    private Node checkIfDirectory(String key) {
        if (key.equals("..")) {
            if (currentNode.parent != null) {
                return currentNode.parent;
            } else {
                return root;
            }
        }

        if (currentNode.children.containsKey(key)) {
            Node toChange = currentNode.children.get(key);
            if (toChange.info.isDirectory) {
                return toChange;
            } else {
                return null;
            }
        } else if (key.equals("root")) {
            return root;
        } else {
            return currentNode;
        }
    }

    public Node changeDirectory(String key) {
        if (key.equals("..")) {
            if (currentNode.parent != null) {
                return currentNode.parent;
            } else {
                return root;
            }
        }

        if (currentNode.children.containsKey(key)) {
            Node toChange = currentNode.children.get(key);
            if (toChange.info.isDirectory) {
                return toChange;
            } else {
                System.out.println(key + ": is not a directory");
                return null;
            }
        } else if (key.equals("root")) {
            return root;
        } else {
            System.out.println(key + ": no such directory");
            return null;
        }
    }

    private Node search(String[] key) {
        Node searchNode = root;

        for (String key1 : key) {
            searchNode = searchNode.children.get(key1);
        }

        return searchNode;

    }
}

class Simulation {

    String hostname;
    String path;
    String prompt;
    Tree tree;
    boolean exit;

    Simulation(Tree tree) {
        this.tree = tree;
        this.hostname = "user@hostname: ";
        this.tree.currentNode = tree.root;
        this.path = tree.currentNode.key;
        this.prompt = "/" + hostname + path + "/ $ ";
        this.exit = false;
    }

    public void showPrompt() {
        path = tree.getPath(tree.currentNode.key, tree.currentNode);
        prompt = hostname + "/" + path + "/ $ ";
        System.out.print(prompt);
    }

    public void takeCommand(String command) throws InterruptedException {
        String cmd, args = "";
        command = command.trim();
        if (command.contains(" ")) {
            cmd = command.substring(0, command.indexOf(" "));
            args = command.substring(command.indexOf(" ") + 1, command.length());
        } else {
            cmd = command;
        }

        switch (cmd) {
            case "ext":
                this.exit = true;
                break;
            case "mkdir":
                makeDirectory(args);
                break;
            case "rmdir":
                removeDirectory(args);
                break;
            case "cd":
                changeDirectory(args);
                break;
            case "ls":
                listChildren(args);
                break;
            case ">":
                createFile(args);
                break;
            case "rm":
                removeFile(args);
                break;
            case ">>":
                appendFile(args);
                break;
            case "edit":
                editFile(args);
                break;
            case "show":
                showFile(args);
                break;
            case "mv":
                move(args);
                break;
            case "rn":
                rename(args);
                break;
            case "cp":
                copy(args);
                break;
            case "":
                break;
            default:
                System.out.println(cmd + ": command not found");
                break;
        }
    }

    private void makeDirectory(String args) {
        if (!args.isEmpty()) {
            String[] address = args.split("/");
            Date date = new Date();
            Node preNode = tree.currentNode;

            if (address[0].equals("root")) {
                tree.currentNode = tree.root;
            }

            tree.makeDirectory(address, date);
            tree.currentNode = preNode;
        } else {
            System.out.println("usage: mkdir <directory name>");
        }
    }

    private void removeDirectory(String args) {
        if (!args.isEmpty()) {
            String[] address = args.split("/");
            Node preNode = tree.currentNode;

            if (address[0].equals("root")) {
                tree.currentNode = tree.root;
            }

            tree.removeDirectory(address);
            tree.currentNode = preNode;
        } else {
            System.out.println("usage: rmdir <directory name>");
        }
    }

    private void changeDirectory(String args) {
        if (!args.isEmpty()) {
            String[] address = args.split("/");
            Node preNode = tree.currentNode;

            if (address[0].equals("root")) {
                tree.currentNode = tree.root;
            }

            for (String addres : address) {
                if (!addres.equals("")) {
                    Node toChange = tree.changeDirectory(addres);
                    if (toChange != null) {
                        tree.currentNode = toChange;
                    } else {
                        tree.currentNode = preNode;
                        break;
                    }
                }
            }
        } else {
            System.out.println("usage: cd <directory name>");
        }

    }

    private void listChildren(String args) {
        if (args.isEmpty()) {
            tree.currentNode.listContents(".*");
        } else if (args.contains("/")) {
            String[] address = args.split("/");
            Node preNode = tree.currentNode;

            if (address[0].equals("root")) {
                tree.currentNode = tree.root;
            }

            for (String addres : address) {
                if (!addres.equals("")) {
                    tree.currentNode = tree.changeDirectory(addres);
                }
            }

            if (tree.currentNode != null) {
                tree.currentNode.listContents(".*");
            }

            tree.currentNode = preNode;
        } else {
            tree.currentNode.listContents(args);
        }
    }

    private void createFile(String args) {
        String[] address = args.substring(args.lastIndexOf(">") + 1,
                args.length()).trim().split("/");
        String key = args.substring(args.lastIndexOf(">") + 1).trim();
        Node preNode = tree.currentNode;

        if (address[0].equals("root")) {
            tree.currentNode = tree.root;
        }

        JFrame frame = new JFrame(key);
        JTextArea txtarFile = new JTextArea(20, 40);
        JScrollPane scrlpaneFile = new JScrollPane(txtarFile);
        JPanel panel = new JPanel();
        JButton save = new JButton("Save");

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.setLayout(new GridLayout(1, 0));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panel.add(scrlpaneFile);
        frame.add(panel);
        panel.add(save);

        save.addActionListener((ActionEvent e) -> {
            saveFileContents(address, txtarFile, frame, true);
        });

        frame.pack();
        frame.setVisible(true);

        tree.currentNode = preNode;

    }

    private void removeFile(String args) {
        String[] address = args.substring(args.lastIndexOf(">") + 1,
                args.length()).trim().split("/");
        String key = args.substring(args.lastIndexOf(">") + 1).trim();
        Node preNode = tree.currentNode;

        if (address[0].equals("root")) {
            tree.currentNode = tree.root;
        }

        tree.removeFile(address);

        tree.currentNode = preNode;
    }

    private void appendFile(String args) {
        String[] address = args.substring(args.lastIndexOf(">") + 1,
                args.length()).trim().split("/");
        Node preNode = tree.currentNode;

        if (address[0].equals("root")) {
            tree.currentNode = tree.root;
        }

        if (address.length > 1) {

            for (int i = 0; i < address.length - 1; i++) {
                if (!address[i].equals("")) {
                    tree.currentNode = tree.changeDirectory(address[i].trim());
                }
            }

            if (tree.currentNode == null) {
                tree.currentNode = preNode;
                return;
            }
        }

        JFrame frame = new JFrame(address[address.length - 1]);
        JTextArea txtarFile = new JTextArea(20, 40);
        JScrollPane scrlpaneFile = new JScrollPane(txtarFile);
        JPanel panel = new JPanel();
        JButton save = new JButton("Save");

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.setLayout(new GridLayout(1, 0));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panel.add(scrlpaneFile);
        frame.add(panel);
        panel.add(save);

        if (tree.currentNode.files.containsKey(address[address.length - 1])) {
            txtarFile.setText(tree.currentNode.files.get(address[address.length - 1]).info.getContents());
        } else {
            System.out.println(address[address.length - 1] + ": no such file");
            tree.currentNode = preNode;
            return;
        }
        txtarFile.setCaretPosition(txtarFile.getText().length());

        save.addActionListener((ActionEvent e) -> {
            saveFileContents(address, txtarFile, frame, false);
        });

        frame.pack();
        frame.setVisible(true);

        tree.currentNode = preNode;

    }

    private void editFile(String args) {
        String[] address = args.substring(args.lastIndexOf(">") + 1,
                args.length()).trim().split("/");
        Node preNode = tree.currentNode;

        if (address[0].equals("root")) {
            tree.currentNode = tree.root;
        }

        if (address.length > 1) {

            for (int i = 0; i < address.length - 1; i++) {
                if (!address[i].equals("")) {
                    tree.currentNode = tree.changeDirectory(address[i].trim());
                }
            }

            if (tree.currentNode == null) {
                tree.currentNode = preNode;
                return;
            }
        }

        JFrame frame = new JFrame(address[address.length - 1]);
        JTextArea txtarFile = new JTextArea(20, 40);
        JScrollPane scrlpaneFile = new JScrollPane(txtarFile);
        JPanel panel = new JPanel();
        JButton save = new JButton("Save");

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.setLayout(new GridLayout(1, 0));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panel.add(scrlpaneFile);
        frame.add(panel);
        panel.add(save);

        if (tree.currentNode.files.containsKey(address[address.length - 1])) {
            txtarFile.setText(tree.currentNode.files.get(address[address.length - 1]).info.getContents());
        } else {
            System.out.println(address[address.length - 1] + ": no such file");
            tree.currentNode = preNode;
            return;
        }

        save.addActionListener((ActionEvent e) -> {
            saveFileContents(address, txtarFile, frame, false);
        });

        frame.pack();
        frame.setVisible(true);

        tree.currentNode = preNode;
    }

    private void saveFileContents(String[] key, JTextArea txtar,
            Window frame, boolean isNewFile) {
        Date date = new Date();
        String contents = "";
        contents = txtar.getText();
        Node preNode = tree.currentNode;

        if (isNewFile) {
            tree.makeFile(key, contents, date);
        } else {
            tree.updateFile(key, contents, date);
        }

        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        tree.currentNode = preNode;
    }

    private void showFile(String args) {
        String[] address = args.substring(args.lastIndexOf(">") + 1,
                args.length()).trim().split("/");
        Node preNode = tree.currentNode;

        if (address[0].equals("root")) {
            tree.currentNode = tree.root;
        }

        if (address.length > 1) {

            for (int i = 0; i < address.length - 1; i++) {
                if (!address[i].equals("")) {
                    tree.currentNode = tree.changeDirectory(address[i].trim());
                }
            }

            if (tree.currentNode == null) {
                tree.currentNode = preNode;
                return;
            }
        }

        JFrame frame = new JFrame(address[address.length - 1]);
        JTextArea txtarFile = new JTextArea(20, 40);
        JScrollPane scrlpaneFile = new JScrollPane(txtarFile);
        JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        frame.setLayout(new GridLayout(1, 0));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        panel.add(scrlpaneFile);
        frame.add(panel);
        txtarFile.setEditable(false);

        if (tree.currentNode.files.containsKey(address[address.length - 1])) {
            txtarFile.setText(tree.currentNode.files.get(address[address.length - 1]).info.getContents());
        } else {
            System.out.println(address[address.length - 1] + ": no such file");
            tree.currentNode = preNode;
            return;
        }

        frame.pack();
        frame.setVisible(true);

        tree.currentNode = preNode;
    }

    private void move(String args) {
        String[] argsSplit = args.split(" ");
        String[] address;
        String filename = argsSplit[0].trim();
        Node preNode = tree.currentNode;

        if (args.trim().contains(" ")) {
            address = argsSplit[1].split("/");

            if (address[0].equals("root")) {
                tree.currentNode = tree.root;
            }

            if (preNode.children.containsKey(filename)) {
                tree.moveDirectory(filename, address, preNode);
            } else if (preNode.files.containsKey(filename)) {
                tree.moveFile(filename, address, preNode);
            } else {
                System.out.println("source_file/source_directory not found");
            }

            tree.currentNode = preNode;
        } else {
            System.out.println("usage: mv source_file/source_directory target_file/target_directory");
        }

        tree.currentNode = preNode;
    }

    private void rename(String args) {
        String[] argsSplit = args.split(" ");
        String filename = argsSplit[0].trim();

        if (args.trim().contains(" ")) {
            String newFilename = argsSplit[1].trim();

            if (tree.currentNode.children.containsKey(filename)) {
                tree.renameDirectory(filename, newFilename);
            } else if (tree.currentNode.files.containsKey(filename)) {
                tree.renameFile(filename, newFilename);
            } else {
                System.out.println("source_file/source_directory not found");
            }

        } else {
            System.out.println("usage: rn <old_filename> <new_filename>");
        }
    }

    private void copy(String args) {
        String[] argsSplit = args.split(" ");
        String[] address;
        String filename = argsSplit[0].trim();
        Node preNode = tree.currentNode;

        if (args.trim().contains(" ")) {
            address = argsSplit[1].split("/");

            if (address[0].equals("root")) {
                tree.currentNode = tree.root;
            }

            if (preNode.children.containsKey(filename)) {
                tree.copyDirectory(filename, address, preNode);
            } else if (preNode.files.containsKey(filename)) {
                tree.copyFile(filename, address, preNode);
            } else {
                System.out.println("source_file/source_directory not found");
            }

            tree.currentNode = preNode;
        } else {
            System.out.println("usage: cp source_file/source_directory target_file/target_directory");
        }

        tree.currentNode = preNode;
    }

}

public class File_system {

    public static void main(String[] args) throws InterruptedException {

        Tree tree = loadTree();
        Simulation simulate = new Simulation(tree);

        do {
            Scanner scan = new Scanner(System.in);
            String command;
            simulate.showPrompt();

            command = scan.nextLine();
            simulate.takeCommand(command);
            saveTree(tree);

        } while (!simulate.exit);

    }

    private static String saveTree(Tree tree) {
        File f = new File("tree.ser");

        try {
            FileOutputStream fileOut = new FileOutputStream(f.getAbsoluteFile());
            ObjectOutputStream output = new ObjectOutputStream(fileOut);
            output.writeObject(tree);
            fileOut.close();
            output.close();
        } catch (IOException i) {
            System.out.println("Error writing file");
        }

        return f.getAbsolutePath();
    }

    private static Tree loadTree() {
        Tree tree = null;

        try {
            FileInputStream fileIn = new FileInputStream("tree.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tree = (Tree) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            System.out.println("No existing file system found \n"
                    + "Creating new file system");
            Date date = new Date();
            Node root = new Node("root", null, date);
            tree = new Tree(root);
        } catch (ClassNotFoundException c) {
            System.out.println("Tree class not found.");
        }

        return tree;
    }
}
