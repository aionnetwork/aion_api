/*******************************************************************************
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 *
 ******************************************************************************/

package org.aion.api.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.aion.api.IMine;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.log.AionLoggerFactory;
import org.aion.api.log.LogEnum;
import org.aion.api.type.ApiMsg;
import org.slf4j.Logger;

/**
 * Created by Jay Tseng on 15/11/16.
 */
public class Mine implements IMine {

    private static final Logger LOGGER = AionLoggerFactory.getLogger(LogEnum.MNE.name());
    private AionAPIImpl apiInst;

    Mine(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    public ApiMsg isMining() {
        if (!this.apiInst.isConnected()) {
            LOGGER.error(
                new Throwable().getStackTrace()[0].getMethodName() + ErrId.getErrString(-1003L));
            return new ApiMsg(-1003);
        }

        byte[] reqHdr = ApiUtils
            .toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_mine, Message.Funcs.f_mining);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);

        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_mining msgRsp = Message.rsp_mining
                .parseFrom(ApiUtils.parseBody(rsp).getData());
            return new ApiMsg(msgRsp.getMining(), ApiMsg.cast.BOOLEAN);

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("[isMining] {} exception: [{}]", ErrId.getErrString(-104L),
                    e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    /*
    public Boolean SubmitWork(Long nonce, byte[] solution, byte[] digest) throws Throwable {
		if (apiInst.m_socket == null)
		{
			throw new NullPointerException("apiInst.m_socket is null");
		}

		Message.req_submitWork reqBody = Message.req_submitWork.newBuilder()
				.setNonce(nonce)
				.setSolution(ByteString.copyFrom(solution))
				.setDigest(ByteString.copyFrom(digest))
				.build();

		byte[] reqHead = ApiUtils.toByteArray(1, Message.Servs.s_mine, Message.Funcs.f_submitWork);
		byte[] req = ByteUtil.merge(reqHead, reqBody.toByteArray());

		if (!apiInst.m_socket.send(req, 0)) {
			throw new IOException("apiInst.m_socket failed to send");
		}

		byte[] respB = apiInst.m_socket.recv();

		if (respB == null ||
				(respB.length == 1 && respB[0] == 0))
		{
			throw new NullPointerException("respB is null");
		}

		try {
			Message.rsp_submitWork resp = Message.rsp_submitWork.parseFrom(respB);
			return resp.getWorkAccepted();
		} catch (InvalidProtocolBufferException e) {
			throw e;
		}
	}
     */
}
