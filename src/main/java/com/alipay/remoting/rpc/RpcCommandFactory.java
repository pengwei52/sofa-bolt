/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.remoting.rpc;

import java.net.InetSocketAddress;

import com.alipay.remoting.CommandFactory;
import com.alipay.remoting.RemotingCommand;
import com.alipay.remoting.ResponseStatus;
import com.alipay.remoting.rpc.exception.RpcServerException;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.remoting.rpc.protocol.RpcResponseCommand;

/**
 * command factory for rpc protocol
 * 
 * @author tsui
 * @version $Id: RpcCommandFactory.java, v 0.1 2018-03-27 21:37 tsui Exp $
 */
@SuppressWarnings("unchecked")
public class RpcCommandFactory implements CommandFactory {
	
	@Override
    public RpcRequestCommand createRequestCommand(Object requestObject) {
        return new RpcRequestCommand(requestObject);
    }

    @Override
    public RpcResponseCommand createResponse(final Object responseObject, final RemotingCommand requestCmd) {
        RpcResponseCommand response = new RpcResponseCommand(requestCmd.getId(), responseObject);
        if (null != responseObject) {
            response.setResponseClass(responseObject.getClass().getName());
        } else {
            response.setResponseClass(null);
        }
        response.setSerializer(requestCmd.getSerializer());
        response.setProtocolSwitch(requestCmd.getProtocolSwitch());
        response.setResponseStatus(ResponseStatus.SUCCESS);
        return response;
    }

    @Override
    public RpcResponseCommand createExceptionResponse(int id, String errMsg) {
        return createExceptionResponse(id, null, errMsg);
    }

    @Override
    public RpcResponseCommand createExceptionResponse(int id, final Throwable t, String errMsg) {
        RpcResponseCommand response = null;
        RpcServerException e = null;
        if (null == t) {
            e = new RpcServerException(errMsg);
            response = new RpcResponseCommand(id, e);
        } else {
            e = new RpcServerException(t.getClass().getName() + ": " + t.getMessage() + ". AdditionalErrMsg: " + errMsg);
            e.setStackTrace(t.getStackTrace());
            response = new RpcResponseCommand(id, e);
        }
        response.setResponseClass(e.getClass().getName());
        response.setResponseStatus(ResponseStatus.SERVER_EXCEPTION);
        return response;
    }

    @Override
    public RpcResponseCommand createExceptionResponse(int id, ResponseStatus status) {
        RpcResponseCommand responseCommand = new RpcResponseCommand();
        responseCommand.setId(id);
        responseCommand.setResponseStatus(status);
        return responseCommand;
    }

    @Override
    public ResponseCommand createTimeoutResponse(InetSocketAddress address) {
        ResponseCommand responseCommand = new ResponseCommand();
        responseCommand.setResponseStatus(ResponseStatus.TIMEOUT);
        responseCommand.setResponseTimeMillis(System.currentTimeMillis());
        responseCommand.setResponseHost(address);
        return responseCommand;
    }

    @Override
    public RemotingCommand createSendFailedResponse(final InetSocketAddress address, Throwable throwable) {
        ResponseCommand responseCommand = new ResponseCommand();
        responseCommand.setResponseStatus(ResponseStatus.CLIENT_SEND_ERROR);
        responseCommand.setResponseTimeMillis(System.currentTimeMillis());
        responseCommand.setResponseHost(address);
        responseCommand.setCause(throwable);
        return responseCommand;
    }

    @Override
    public RemotingCommand createConnectionClosedResponse(InetSocketAddress address, String message) {
        ResponseCommand responseCommand = new ResponseCommand();
        responseCommand.setResponseStatus(ResponseStatus.CONNECTION_CLOSED);
        responseCommand.setResponseTimeMillis(System.currentTimeMillis());
        responseCommand.setResponseHost(address);
        return responseCommand;
    }
}