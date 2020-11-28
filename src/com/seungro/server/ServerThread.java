package com.seungro.server;

import com.seungro.data.Unit;
import com.seungro.server.utils.ClientData;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
    private Socket socket;
    private ArrayList<ClientData> members;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String id;
    private String auth;
    private ClientData me;
    private int type;

    public ServerThread(Socket socket, ArrayList<ClientData> members, String auth) {
        this.socket = socket;
        this.members = members;
        this.auth = auth;
    }

    public void run() {
        System.out.println("[" + socket.getInetAddress() + "] 접속");

        while(true) {
            try {
                input = new ObjectInputStream(socket.getInputStream());

                Unit u = (Unit) input.readObject();

                type = u.getType();
                id = u.getUserName();

                me = new ClientData(id, socket);

                if(type == Unit.ENTER_DATA) {
                    String value = (String) u.getValue();
                    String[] receive = value.split("/");
                    String roomKey = value.replace(receive[0] + "/", "");
                    System.out.println(receive[0] + "/" + roomKey);

                    if (receive[0].equals("new")) {
                        synchronized (members) {
                            members.add(me);
                            receive(new Unit(Unit.LOG_DATA, null, roomKey, null));
                        }
                    }

                    if (receive[0].equals("join")) {
                        synchronized (members) {
                            int i = 2;
                            for (ClientData member : members) {
                                if (member.getName().equals(me.getName())) {
                                    me.setName(id + " " + i);
                                }

                                i++;
                            }

                            members.add(me);

                            ArrayList<String> mems  = new ArrayList<String>();

                            for(ClientData member : members) {
                                mems.add(member.getName());
                            }

                            System.out.println("[SERVER] auth = " + members.get(0).getName());
                            receive(new Unit(Unit.LOG_DATA, members.get(0).getName(), "success", mems));
                            broadcast(new Unit(Unit.LOG_DATA, me.getName(), "join", mems));
                        }
                    }
                } else {
                    broadcast(u);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("[" + socket.getInetAddress() + "] 연결종료");

                members.remove(me);

                broadcast(new Unit(0, id, null, "disconnect"));

                try {
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;
            }
        }
    }

    private void receive(Unit u) {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(u);
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcast(Unit u) {
        try {
            for (ClientData member : members) {
                System.out.println("[SERVER] broadcast to " + member.getName());
                if (member.getSocket().equals(me.getSocket())) {
                    System.out.println("[SERVER] it's me");
                    continue;
                }

                output = new ObjectOutputStream(member.getSocket().getOutputStream());
                output.writeObject(u);
                output.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
