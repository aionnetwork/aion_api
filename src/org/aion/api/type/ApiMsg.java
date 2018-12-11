package org.aion.api.type;

import org.aion.api.impl.ErrId;

/**
 * The return type of every API call. Retrieve the actual return object by {@link
 * org.aion.api.type.ApiMsg#getObject() getObject}. To interact with the actual return object, see
 * the documentation of the return type. Before getting the return object, use {@link
 * org.aion.api.type.ApiMsg#isError() isError} to check if an isError occurred. If an isError has
 * has occurred, use {@link org.aion.api.type.ApiMsg#getErrString() getErrString} to get the
 * detailed isError message.
 *
 * @author Jay Tseng
 */
public class ApiMsg {

    // the Message return code. When everything correct. This code will be set to 1.
    private int errorCode;

    // the Aion network kernel returns the data wrapped as a Object.
    private Object rtnObj;

    // cast type:
    // 0: boolean
    // 1: int
    // 2: long
    // 3: Others
    private cast castType;

    public ApiMsg() {
        this.errorCode = 0;
        this.rtnObj = null;
        this.castType = cast.NULL;
    }

    public ApiMsg(int code) {
        this.errorCode = code;
        this.rtnObj = null;
    }

    public ApiMsg(int code, Object obj) {
        this.errorCode = code;
        this.rtnObj = obj;
    }

    public ApiMsg(Object obj, cast type) {
        this.errorCode = 1;
        this.rtnObj = obj;
        this.castType = type;
    }

    public ApiMsg(int code, Object obj, cast type) {
        this.errorCode = code;
        this.rtnObj = obj;
        this.castType = type;
    }

    public boolean isError() {
        return (this.getErrorCode() < 1 || this.getErrorCode() == 102);
    }

    public ApiMsg set(int code) {
        return set(code, null, cast.NULL);
    }

    public ApiMsg set(int code, Object obj, cast type) {
        this.errorCode = code;
        this.rtnObj = obj;
        this.castType = type;
        return this;
    }

    public ApiMsg set(Object obj, cast type) {
        return set(1, obj, type);
    }

    public ApiMsg set(ApiMsg msg) {
        this.errorCode = msg.getErrorCode();
        this.rtnObj = msg.getObject();
        this.castType = msg.getCastType();
        return this;
    }

    @SuppressWarnings("unchecked")
    public <Any> Any getObject() {
        switch (this.castType) {
            case BOOLEAN:
                return (Any) ((Boolean) (boolean) this.rtnObj);
            case INT:
                return (Any) ((Integer) (int) this.rtnObj);
            case LONG:
                return (Any) ((Long) (long) this.rtnObj);
            default:
                return (Any) (this.rtnObj);
        }
    }

    private cast getCastType() {
        return this.castType;
    }

    public String getErrString() {
        return ErrId.getErrString(this.getErrorCode());
    }

    public int getErrorCode() {
        return errorCode;
    }

    public enum cast {
        BOOLEAN,
        INT,
        LONG,
        OTHERS,
        NULL
    }
}
