Fabric 环境搭建

1. 安装docker/golang，略

2. 清理旧镜像
  docker stop $(docker ps -a -q)
  # docker rm $(docker ps -a -q)
  # docker rmi -f $(docker images |grep "dev-" |awk '{print $3}')

  # remove all stopped containers.
  docker container prune
  # remove all images without at least one container associated to them.
  docker image prune -a
  # remove all volumes not used by at least one container.
  docker volume prune

3. 拉取镜像
  docker pull hyperledger/fabric-zookeeper:amd64-0.4.14
  docker tag hyperledger/fabric-zookeeper:amd64-0.4.14 hyperledger/fabric-zookeeper

  docker pull hyperledger/fabric-kafka:amd64-0.4.14
  docker tag hyperledger/fabric-kafka:amd64-0.4.14 hyperledger/fabric-kafka

  docker pull hyperledger/fabric-orderer:amd64-1.3.0
  docker tag hyperledger/fabric-orderer:amd64-1.3.0 hyperledger/fabric-orderer

  docker pull hyperledger/fabric-ca:amd64-1.3.0
  docker tag hyperledger/fabric-ca:amd64-1.3.0 hyperledger/fabric-ca

  docker pull hyperledger/fabric-couchdb:amd64-0.4.14
  docker tag hyperledger/fabric-couchdb:amd64-0.4.14 hyperledger/fabric-couchdb

  docker pull hyperledger/fabric-peer:amd64-1.3.0
  docker tag hyperledger/fabric-peer:amd64-1.3.0 hyperledger/fabric-peer

  docker pull hyperledger/fabric-ccenv:amd64-1.3.0
  docker tag hyperledger/fabric-ccenv:amd64-1.3.0 hyperledger/fabric-ccenv

  docker pull hyperledger/fabric-javaenv:amd64-1.3.0
  docker tag hyperledger/fabric-javaenv:amd64-1.3.0 hyperledger/fabric-javaenv

  docker pull hyperledger/fabric-tools:amd64-1.3.0
  docker tag hyperledger/fabric-tools:amd64-1.3.0 hyperledger/fabric-tools

4. 生成公私钥、证书、创世区块
  4.1 编辑crypto-config.yaml
  4.2 编辑configtx.yaml
  4.3 下载工具
    https://nexus.hyperledger.org/content/repositories/releases/org/hyperledger/fabric/hyperledger-fabric/linux-amd64-1.3.0/hyperledger-fabric-linux-amd64-1.3.0.tar.gz
    解压得到bin/目录
  4.4 生成公私钥和证书
    ./bin/cryptogen generate --config=./crypto-config.yaml
  4.5 生成创世区块
    ./bin/configtxgen -profile OneOrgsOrdererGenesis -outputBlock ./channel-artifacts/genesis.block -channelID=system.channel
  4.6 由于不需要搭建cli环境，所以channel应该不需要事先定义
    ./bin/configtxgen -outputCreateChannelTx public.channel.tx -profile OneOrgsChannel -channelID public.channel

5. 部署zookeeper/kafka/orderer
  略

6. 部署ca
  编辑docker-compose-ca.yaml
  pem文件名和sk文件名根据4.4中生成的文件名做相应的替换
  command命令中-b参数绑定了管理员用户和密码，如需修改可在此替换

7. 部署peer
  environment中需添加如下参数：
  # the following setting starts chaincode containers on the same bridge network as the peers
  - CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE=xxxxx
  这个参数用来解决instantiate chaincode时，peer节点报Timeout expired while starting chaincode错误

8. 其他常用调试命令
  关闭服务并清除数据
  docker-compose down --remove-orphans
  删除已部署的链码
  docker rmi -f $(docker images |grep "dev-" |awk '{print $3}')
  启动服务
  docker-compose -f docker-compose-peer.yaml up -d
  跟踪日志
  docker logs --follow ca0.ulebc.io

9. 项目中需要配置的证书私钥等文件来源
  org/Admin@dev.jerry.cn-cert.pem <-- crypto-config/peerOrganizations/dev.jerry.cn/users/Admin@dev.jerry.cn/msp/signcerts/Admin@dev.jerry.cn-cert.pem
  org/Admin@dev.jerry.cn-key_sk   <-- crypto-config/peerOrganizations/dev.jerry.cn/users/Admin@dev.jerry.cn/msp/keystore/d9f7af4171cad95c8d370a8ba70fe28fa38c079908b39b1e79725821e8750ed4_sk
  tls/ca.dev.jerry.cn-cert.pem    <-- crypto-config/peerOrganizations/dev.jerry.cn/ca/ca.dev.jerry.cn-cert.pem
  tls/orderer0.dev.jerry.cn.crt   <-- crypto-config/ordererOrganizations/dev.jerry.cn/orderers/orderer0.dev.jerry.cn/tls/server.crt
  tls/peer0.dev.jerry.cn.crt      <-- crypto-config/peerOrganizations/dev.jerry.cn/peers/peer0.dev.jerry.cn/tls/server.crt
  tls/peer1.dev.jerry.cn.crt      <-- crypto-config/peerOrganizations/dev.jerry.cn/peers/peer1.dev.jerry.cn/tls/server.crt
