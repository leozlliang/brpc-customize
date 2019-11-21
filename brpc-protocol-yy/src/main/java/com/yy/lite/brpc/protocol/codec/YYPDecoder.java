/*
 * Copyright (c) 2014 yy.com. 
 *
 * All Rights Reserved.
 *
 * This program is the confidential and proprietary information of 
 * YY.INC. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with yy.com.
 */
package com.yy.lite.brpc.protocol.codec;

import com.yy.anka.io.codec.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;

/**
 * @author Tingkun Zhang
 */
public final class YYPDecoder extends LengthFieldBasedFrameDecoder {

    private final static Logger LOGGER = LoggerFactory.getLogger(YYPDecoder.class);

    public YYPDecoder() {
        // 本来应该是4，jsonp的存在占了一位
        super(ByteOrder.LITTLE_ENDIAN, PacketUtils.DEFAULT_PACKET_LIMIT_SIZE, 0, 3, -3, 0, true);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (frame == null) {
                return null;
            }
            return frame;
        } finally {
            if (frame != null) {
                frame.release();
            }
        }
    }

}
