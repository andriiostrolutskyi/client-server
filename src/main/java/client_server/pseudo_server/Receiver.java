package client_server.pseudo_server;


import client_server.Message;
import client_server.MessagePacket;
import client_server.Decryptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Receiver {
    public ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void receiveMessage(ArrayList<byte[]> encryptedByteMessagePackets) throws ExecutionException, InterruptedException {
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < encryptedByteMessagePackets.size(); i++) {
            int finalI = i;
            Future<?> future = threadPool.submit(() -> {
                try {
                    MessagePacket messagePacket = Decryptor.parsePacket(encryptedByteMessagePackets.get(finalI));
                    Message m = messagePacket.getMessage();
                    Processor.process(m);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
        }

        threadPool.shutdown();
        for (Future<?> future : futures) {
            future.get();
        }
    }
}


