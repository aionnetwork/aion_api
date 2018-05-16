package org.aion.api.test;

import static org.junit.Assert.assertFalse;

import org.aion.api.IAionAPI;
import org.aion.api.type.ApiMsg;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConnectingTests {

    static IAionAPI api = IAionAPI.init();

    @Test
    public void TestApiConnect() {
        System.out.println("run TestApiConnect.");

        ApiMsg apiMsg = api.connect(IAionAPI.LOCALHOST_URL);

        assertFalse(apiMsg.isError());
        api.destroyApi();

        System.out.println("run TestApiConnect again.");
        apiMsg = api.connect(IAionAPI.LOCALHOST_URL);
        assertFalse(apiMsg.isError());

        api.destroyApi();
    }
}
