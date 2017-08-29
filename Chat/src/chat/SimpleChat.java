package chat;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleChat extends ReceiverAdapter {
    
    JChannel channel;
    String historico = "";
    UI ui;
    String aux = "";
    String user_name = System.getProperty("user.name", "n/a");
    final List<String> state = new LinkedList<String>();
    
    public SimpleChat(UI ui) {
        this.ui = ui;
        
    }
    
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }
    
    public void receive(Message msg) {
        String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
        historico += line + "\n";
        ui.getjTextPane1().setText(historico);
        
        synchronized (state) {
            state.add(line);
        }
    }
    
    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }
    
    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception {
        List<String> list = (List<String>) Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println("received state (" + list.size() + " messages in chat history):");
        for (String str : list) {
            System.out.println(str);
            historico +=  str + "\n";
        }
        ui.getjTextPane1().setText(historico);
    }
    
    public void start(String canal) throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect(canal);
        channel.getState(null, 10000);
        eventLoop(channel);
    }
    
    
    public void enviarMsg(String msgCrua) throws Exception {
        String msgCompleta = "";
        msgCompleta = "[" + user_name + "] :" + msgCrua + "\n"; //Adicionando nome do remetente à mensagem
        //historico += msgCompleta; //Atualizando o histórico da conversa
        Message msg = new Message(null, null, msgCompleta);
        channel.send(msg);
    }
    
    private void eventLoop(JChannel canal) {
        ui.getjButton2().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canal.close();
                ui.setDisc();
                ui.getjTextPane1().setText("");
            }
        });
        ui.getjButton1().addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    enviarMsg(ui.getjTextField1().getText());
                    ui.getjTextField1().setText("");
                    //ui.getjTextPane1().setText(historico);
                } catch (Exception ex) {
                    
                }
            }
        });

        /*while (true) {
         try {
                
         String line = in.readLine().toLowerCase();
         if (line.startsWith("quit") || line.startsWith("exit")) {
         break;
         }
         line = "[" + user_name + "] " + line;
         Message msg = new Message(null, null, line);
         channel.send(msg);
         } catch (Exception e) {
         }
         }*/
    }

    public String getHistorico() {
        return historico;
    }
    
}
