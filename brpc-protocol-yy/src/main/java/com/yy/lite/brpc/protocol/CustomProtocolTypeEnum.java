package com.yy.lite.brpc.protocol;

/**
 * @author donghonghua
 * @date 2019/7/23
 */
public enum  CustomProtocolTypeEnum {
    /**
     * yyp协议
     */
    YYP(100);

    public int type;

    CustomProtocolTypeEnum(int type) {
        this.type = type;
    }
}
