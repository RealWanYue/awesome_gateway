package com.arise.modules;

import com.arise.internal.chain.ChainContext;

import java.io.IOException;

/**
 * @Author: wy
 * @Date: Created in 14:07 2021-03-01
 * @Description: 应用协议解析
 * @Modified: By：
 */
public interface ProtocolHandler {

    void handleRequest(ChainContext ctx, Object msg);
}
