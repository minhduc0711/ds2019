package naming;

import storage.Storage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class DirectoryTreeNode {
    private HashMap<String, DirectoryTreeNode> children;
    private boolean isDir;
    private List<Storage> storageList;
    private String currentPath;
    private DirectoryTreeNode parent;

    public DirectoryTreeNode(String currentPath) {
        children = new HashMap<>();
        isDir = true;
        storageList = null;
        parent = null;

        this.currentPath = currentPath;
    }

    public void setIsDir(boolean isDir) {
        this.isDir = isDir;
    }

    public void addChild(String name, DirectoryTreeNode child) {
        children.put(name, child);
        child.parent = this;
    }

    public DirectoryTreeNode getLastNodeInPath(Path path) {
        DirectoryTreeNode currentNode = this;
        Iterator<Path> pathIterator = path.iterator();
        while (pathIterator.hasNext()) {
            String childName = pathIterator.next().toString();
            if (currentNode.getChildren().containsKey(childName)) {
                currentNode = currentNode.getChildren().get(childName);
            } else {
                return null;
            }
        }
        return currentNode;
    }

    private boolean addPath(Iterator<Path> pathIterator, boolean isDir, Storage storage) {
        if (pathIterator.hasNext()) {
            String nodeName = pathIterator.next().toString();
            if (this.children.containsKey(nodeName)) {
                DirectoryTreeNode nextNode = children.get(nodeName);
                return nextNode.addPath(pathIterator, isDir, storage);
            } else {
                DirectoryTreeNode currentNode = this;
                while (true) {
                    DirectoryTreeNode newNode = new DirectoryTreeNode(Paths.get(currentNode.currentPath, nodeName).toString());
                    currentNode.addChild(nodeName, newNode);
                    if (!pathIterator.hasNext()) {
                        newNode.setIsDir(isDir);
                        if (!isDir) {
                            if (newNode.storageList == null) newNode.storageList = new ArrayList<>();
                            newNode.storageList.add(storage);
                        }
                        break;
                    }
                    currentNode = newNode;
                    nodeName = pathIterator.next().toString();
                }
                return true;
            }
        }
        return false;
    }

    public boolean addPath(Path p, boolean isDir, Storage storage) {
        return addPath(p.iterator(), isDir, storage);
    }

    public HashMap<String, DirectoryTreeNode> getChildren() {
        return children;
    }

    public List<Storage> getStorageList() {
        return storageList;
    }

    public DirectoryTreeNode getParent() {
        return parent;
    }

    public boolean isDir() {
        return isDir;
    }

    @Override
    public String toString() {
        return currentPath;
    }
}
