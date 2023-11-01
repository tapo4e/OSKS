package com.example.ports;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;



import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Serialports implements SerialPortDataListener {
    private  SerialPort serialPort;
    private String output=null;
    private OutputStream outputStream;
    private int counterByte;

    private final List<String> list=new ArrayList<>();

    private final SerialPort[] serialPorts = SerialPort.getCommPorts();

//#g-flag
    //$-esc
    //&-change flag
    //!change esc
    public  Serialports() {
        for (SerialPort port : serialPorts) {
            port.openPort();
            if (port.isOpen()) {
                serialPort=port;
                break;
            }
        }
        if(serialPort==null){
            System.exit(0);
        }
        open();
    }
    public String sendStringToComm(String command) throws IOException {
//        StringBuilder string= new StringBuilder(command);
//        for(int i=0;i<string.length();i++){
//            if(string.charAt(i)=='$'){
//                string.deleteCharAt(i);
//                string.insert(i,"$!");
//            }
//            else if (string.charAt(i)=='#' && string.charAt(i+1)=='g'){
//                string.deleteCharAt(i);
//                string.deleteCharAt(i+1);
//                string.insert(i,"$&");
//            }
//        }
        command=command.replace("$","$!");
        command=command.replace("#g","$&");
        byte b=(byte)Integer.parseInt(serialPort.getSystemPortName().replace("COM",""));
        String str=String.format("%s%s%s%c","#g0","1", command,'0');
        byte[] bytes=str.getBytes("windows-1251");
        bytes[3]=b;
        outputStream.write(bytes);
        str=str.replaceFirst("1",String.format("%x",b));
        System.out.println(str);
        return str;
    }

    @Override
    public int getListeningEvents() {
        return  SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }
    @Override
    public void serialEvent(SerialPortEvent event) {
        byte[] input = new byte[serialPort.bytesAvailable()];
        counterByte=serialPort.bytesAvailable();
        serialPort.readBytes(input,serialPort.bytesAvailable());
        try {
            output = new String(input, "windows-1251");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        output=output.substring(4,output.length()-1);
//        StringBuilder str= new StringBuilder(output);
//        for(int i=0;i<str.length();i++){
//            if(str.charAt(i)=='$' && str.charAt(i+1)=='&'){
//                str.deleteCharAt(i);
//                str.deleteCharAt(i+1);
//                str.insert(i,"#g");
//            }
//            else if (str.charAt(i)=='$' && str.charAt(i+1)=='!'){
//                str.deleteCharAt(i);
//                str.deleteCharAt(i+1);
//                str.insert(i,"$");
//            }
//        }
//            output=str.toString();
       output=output.replace("$&","#g");
        output=output.replace("$!","$");
    }



    public String getOutput() {
        return output;
    }

    public String getCounterByte(){
        return String.valueOf(counterByte);
    }
    public SerialPort getSerialPort() {
        return serialPort;
    }



    public void setOutput() {
        output = null;
    }

    public String getBaudRate(){
        return  String.valueOf(serialPort.getBaudRate());
    }

    public List<String> getList(){
        for(SerialPort port : serialPorts){
            //if(Integer.parseInt(port.getSystemPortName().replaceAll("COM",""))<15) {
                list.add(port.getSystemPortName().replaceAll("COM", ""));
            //}
        }
        return list;
    }
    public  Boolean OpenPort(String name){
        SerialPort port = SerialPort.getCommPort(name);
        port.openPort();
        if(!port.isOpen()){
            System.out.println("false1");
            return false;
        }
        else {
            serialPort.closePort();
            serialPort=port;
            open();
            return  true;
        }
    }
    private void open(){
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setNumDataBits(8);
        serialPort.addDataListener(this);
        serialPort.setBaudRate(9600);
        outputStream=serialPort.getOutputStream();
        counterByte = 0;
    }

}
