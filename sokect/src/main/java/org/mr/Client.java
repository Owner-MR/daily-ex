package org.mr;

import org.json.simple.JSONObject;
import org.junit.internal.runners.statements.RunAfters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    public static boolean connection_state = false;

    public static void main(String[] args) {
        connect();

    }

    private static void connect() {

        try {
            socket = new Socket("127.0.0.1", 9999);
            connection_state = true;
            System.out.println("用户端运行中.........");
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            new Thread(new Client_heart(socket, oos)).start();
            new Thread(new Client_listen(socket)).start();
            new Thread(new Client_send(socket, oos)).start();

        } catch (Exception e) {
            e.printStackTrace();
            connection_state = false;
        }
    }

    public static void reconnect() {
        while (!connection_state) {
            System.out.println("正在尝试重新连接.......");
            connect();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Client_listen implements Runnable {
    private Socket socket;

    public Client_listen(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            while (true)
                System.out.println(ois.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

class Client_send implements Runnable {
    private Socket socket;
    private ObjectOutputStream oos;

    public Client_send(Socket socket, ObjectOutputStream oos) {
        this.socket = socket;
        this.oos = oos;
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                JSONObject jsonObject = new JSONObject();
                System.out.print("请输入要发送的消息：");
                String msg = scanner.nextLine();
                jsonObject.put("type", "chat");
                jsonObject.put("msg", msg);
                oos.writeObject(jsonObject);
                oos.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Client.connection_state = false;
        }
    }
}

class Client_heart implements Runnable {
    private ObjectOutputStream oos;
    private Socket socket;

    public Client_heart(Socket socket, ObjectOutputStream oos) {
        this.socket = socket;
        this.oos = oos;
    }
    @Override
    public void run() {
        try {
            System.out.println("心跳包线程已启动....");
            while (true) {
                Thread.sleep(5000);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "heart");
                jsonObject.put("msg", "心跳包");
                oos.writeObject(jsonObject);
                oos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Client.connection_state = false;
                Client.reconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
