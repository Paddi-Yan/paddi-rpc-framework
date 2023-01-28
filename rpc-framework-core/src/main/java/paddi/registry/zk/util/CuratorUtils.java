package paddi.registry.zk.util;

import com.paddi.enums.RpcConfigType;
import com.paddi.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Curator(zookeeper client) utils
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 13:20:14
 */
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "paddi-rpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    private CuratorUtils() { }

    /**
     * create persistent nodes. Unlike temporary nodes, persistent nodes are not removed when the client disconnect
     * @param zkClient
     * @param path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if(REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already exists, The node is: [{}]", path);
            }else {
                //eg: /paddi-rpc/com.paddi.MessageService/127.0.0.1:8888
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                REGISTERED_PATH_SET.add(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
        }catch(Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    /**
     * Get the children under a node
     * @param rpcServiceName rpc service name eg:com.paddi.MessageServiceGroup2Version1
     * @return all child nodes under the specified node
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        }catch(Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * Empty the registry of data
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
       REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
           try {
               if(p.endsWith(inetSocketAddress.toString())) {
                   zkClient.delete().forPath(p);
               }
           } catch(Exception e) {
               log.error("clear registry for path [{}] fail", p);
           }
       });
       log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET);
    }

    public static CuratorFramework getZkClient() {
        //if zkClient has been started, return directly
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        //check if user has set zk address
        Properties rpcProperties = PropertiesFileUtil.readPropertiesFile(RpcConfigType.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress
                = (rpcProperties != null && rpcProperties.getProperty(RpcConfigType.ZK_ADDRESS.getPropertyValue()) != null)
                ? rpcProperties.getProperty(RpcConfigType.ZK_ADDRESS.getPropertyValue())
                : DEFAULT_ZOOKEEPER_ADDRESS;

        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                 //the zookeeper service addresses (can be a server list)
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("waiting timeout to connect the zookeeper");
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * register to listen for changes to the specified node
     * @param rpcServiceName rpc service name
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = ((curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        });
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

}
