package part1.Client.serviceCenter.ZkWatcher;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import part1.Client.cache.ServiceCache;

public class WatchZK {

    private final CuratorFramework client;
    private final ServiceCache serviceCache;

    public WatchZK(CuratorFramework client, ServiceCache serviceCache) {
        this.client = client;
        this.serviceCache = serviceCache;
    }

    public void watch() {
        CuratorCache curatorCache = CuratorCache.build(client, "/");

        curatorCache.listenable().addListener((type, oldData, newData) -> {
            switch (type) {
                case NODE_CREATED:
                    if (newData != null) {
                        handleAdd(newData.getPath());
                    }
                    break;

                case NODE_DELETED:
                    if (oldData != null) {
                        handleDelete(oldData.getPath());
                    }
                    break;

                case NODE_CHANGED:
                    if (oldData != null && newData != null) {
                        handleUpdate(oldData.getPath(), newData.getPath());
                    }
                    break;

                default:
                    break;
            }
        });

        curatorCache.start();
    }

    private void handleAdd(String path) {
        String[] parts = path.split("/");

        if (parts.length >= 3) {
            serviceCache.addService(parts[1], parts[2]);
        }
    }

    private void handleDelete(String path) {
        String[] parts = path.split("/");

        if (parts.length >= 3) {
            serviceCache.removeService(parts[1], parts[2]);
        }
    }

    private void handleUpdate(String oldPath, String newPath) {
        String[] oldParts = oldPath.split("/");
        String[] newParts = newPath.split("/");

        if (oldParts.length >= 3 && newParts.length >= 3) {
            serviceCache.updateService(oldParts[1], oldParts[2], newParts[2]);
        }
    }
}