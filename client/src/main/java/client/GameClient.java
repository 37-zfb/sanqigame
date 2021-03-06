package client;

import client.cmd.ResultHandlerFactory;
import client.cmd.UserLoginCmdClient;
import client.cmd.UserRegisterCmdClient;
import client.model.SceneData;
import client.model.User;
import client.scene.ExcelReaderUtil;
import client.scene.GameData;
import codoc.GameMsgDecoder;
import codoc.GameMsgEncoder;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import msg.GameMsg;
import msg.GameMsgRecognizer;

import java.net.URI;
import java.util.Scanner;

/**
 * @author 张丰博
 */
@Slf4j
public class GameClient {
    static final String URL = System.getProperty("url", "ws://127.0.0.1:8080/websocket");

    public static void main(String[] args) throws Exception {
        GameMsgRecognizer.init();
        ResultHandlerFactory.init();
        ExcelReaderUtil.ReadData();
        GameData.getInstance().initGameData();
        SceneData.getInstance().init();


        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }


        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final GameClientHandler handler =
                    new GameClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();

                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(8192),
                                    WebSocketClientCompressionHandler.INSTANCE,
                                    new LoggingHandler(),
                                    new GameMsgEncoder(),
                                    new GameMsgDecoder(),
                                    handler);
                        }
                    });

            ChannelFuture channelFuture = b.connect(uri.getHost(), port).sync();
            Channel ch = channelFuture.channel();


            // 握手
            handler.handshakeFuture().sync();

            cmdLogin(ch);

            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }


    }


    public static void cmdLogin(Channel ch){
        Scanner scanner = new Scanner(System.in);
        // 根据用户指令选择：登录 or 注册
        User loginOrReg = null;
        while (true) {
            int comCode = -1;
            while (true) {
                log.info("============请选择您的操作:==============");
                System.out.println("1、登录!");
                System.out.println("2、注册!");
                comCode = scanner.nextInt();
                if (comCode == 1) {
                    // 登录
                    UserLoginCmdClient loginCmdClient = new UserLoginCmdClient();
                    loginOrReg = loginCmdClient.login();

                    break;
                } else if (comCode == 2) {
                    // 注册
                    UserRegisterCmdClient registerCmdClient = new UserRegisterCmdClient();
                    loginOrReg = registerCmdClient.register();

                    break;
                } else {
                    log.info("===> 命令输入有误!");
                }
            }

            GeneratedMessageV3 cmd = null;
            if (comCode == 1) {
                // 登录 指令
                cmd = GameMsg.UserLoginCmd.newBuilder()
                        .setUserName(loginOrReg.getUserName())
                        .setPassword(loginOrReg.getPassword())
                        .build();
            } else if (comCode == 2) {
                // 注册指令
                cmd = GameMsg.UserRegisterCmd.newBuilder()
                        .setNewUserName(loginOrReg.getUserName())
                        .setNewPassword(loginOrReg.getPassword())
                        .setProfessionId(loginOrReg.getProfessionId())
                        .build();
//                }

                // 获得消息类型
                Class<?> msgClass = cmd.getClass();
                // 根据类型获得消息编码
                int msgCode = GameMsgRecognizer.getMsgCodeByMsgClass(msgClass);
                if (msgCode < 0) {
                    log.error("无法识别的消息类型:{}", msgClass);
                    return;
                }
            }
            ch.writeAndFlush(cmd);
            break;
        }
    }





}
