/*
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
 */

/**
 * This test will try to see how many api can be connected to
 * a single aion node. We will try use as many instance as possible to
 * connect to the server @ "tcp://127.0.0.1:8547"
 */

package org.aion.api.test;

import org.aion.api.IAionAPI;
//import org.aion.api.sol.impl.Address;
import org.aion.api.type.ApiMsg;
import org.aion.base.type.Address;


import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class ApiClientConnection2Test {
    public static String url = "tcp://127.0.0.1:8547";
    public static int ccc = 0;

    public static ConcurrentHashMap<IAionAPI, ApiMsg> map = new ConcurrentHashMap<>();

    public static void main(String args[]) throws InterruptedException {
        Scanner scan = new Scanner(System.in);
        System.out.println("================Testing how many calls an Aion node can handle================");

        // check available processors
        int numberOfAvailableProcessors = Runtime.getRuntime().availableProcessors();
        //int processorsToUse = numberOfAvailableProcessors - 2;
        int processorsToUse = numberOfAvailableProcessors * 2; //IMPORTANT FOR INPUT, MUST BE DIVISIBLE
        System.out.println("number of processors available: " + numberOfAvailableProcessors);

        while(true){
            int num;
            System.out.println("Enter the number of new instance you would like to create, or enter \"exit\" to stop");
            String numberOfNewInstances = scan.nextLine();

            if (numberOfNewInstances.equals("exit"))
                break;

            try{
                num = Integer.parseInt(numberOfNewInstances);
            }catch (NumberFormatException e){
                num = 0;
            }

            List<CreateInstances2> creators;
            // give 4, 1 to each
            if(num < 10){
                creators = new ArrayList<>(processorsToUse);
                for (int i = 0; i < 4; i++){
                    CreateInstances2 creator = new CreateInstances2("Thread" + i, 1);
                    creators.add(creator);
                    creator.start();
                }

                synchronized (creators.get(0)){
                    creators.get(0).wait();
                }
            }

            // loop through alot
            else{
                // divide up the workload
                int count = num/processorsToUse;

                // create api instances
                creators = new ArrayList<>(processorsToUse);

                for (int i = 0; i < processorsToUse; i++){
                    CreateInstances2 creator = new CreateInstances2("Thread" + i, count);
                    creators.add(creator);
                    creator.start();
                    TimeUnit.MILLISECONDS.sleep(10);
                }

                for(int i = 0; i < creators.size(); i++){
                    creators.get(i).join();
                    creators.get(i).stop();
                }
            }
            System.out.println("                                                                                    Press enter to execute and call server");
            scan.nextLine();
            callServer2();
        }

        // close api instances
        System.out.println("Press enter to close all api and exit");
        scan.nextLine();
        closeAllApi2();

        exit(0);
    }

    private static void callServer2(){
        System.out.println("====Calling Server====");

        int counter = 0;
        Address address = Address.wrap("0xa08fc457b39b03c30dc71bdb89a4d0409dd4fa42f6539a5c3ee4054af9b71f23");
        for(ConcurrentHashMap.Entry<IAionAPI, ApiMsg> entry: map.entrySet()){

            //entry.getValue().set(entry.getKey().getChain().getBalance(address));
            BigInteger accountBalance = entry.getKey().getChain().getBalance(address).getObject();

            //entry.getValue().set(entry.getKey().getChain().blockNumber());
            long blockNumber = entry.getKey().getChain().blockNumber().getObject();

            System.out.println("entry >>>" + entry);
            System.out.println("Connections number " + counter + ", block number " + blockNumber);
            System.out.println("Connections number " + counter + ", accountBalance " + accountBalance);

            counter++;
        }
        System.out.println("====Calling Server Finish====");
    }
    /**
     * Loop through each instance and destroy it
     */
    private static void closeAllApi2(){
        for(ConcurrentHashMap.Entry<IAionAPI, ApiMsg> entry: map.entrySet()){
            entry.getKey().destroyApi();
        }
    }
}

class CreateInstances2 extends Thread{
    private String threadName;
    private int numInstances;

    CreateInstances2(String name, int numbInstances){
        this.threadName = name;
        this.numInstances = numbInstances;
        System.out.println("Created thread: " + threadName + " to instantiate " + this.numInstances + " instances");
    }

    public void run(){
        boolean running = true;
        // create instances
        while(running) {
            for (int i = 0; i < numInstances; i++) {
                IAionAPI newApiInstance;
                try {
                    newApiInstance = generateNewAPIInstance();
                } catch (OutOfMemoryError error) {
                    return;
                }

                ApiMsg newApiMsg;
                try {
                    newApiMsg = newApiInstance.connect(ApiClientConnection2Test.url);
                } catch (OutOfMemoryError error) {
                    newApiMsg = null;
                    return;
                }

                if (!newApiMsg.isError()) {
                    ApiClientConnection2Test.map.put(newApiInstance, newApiMsg);
                    System.out.println("                                        " +
                            "New api created by " + this.threadName + " Total api: " +
                            ApiClientConnection2Test.map.size() + " Total thread count: " + Thread.activeCount());
                    ApiClientConnection2Test.ccc++;
                }
            }
            running = false;
        }
    }

    public static IAionAPI generateNewAPIInstance(){
        IAionAPI newAPI;
        newAPI = IAionAPI.init();
        return newAPI;
    }
}
