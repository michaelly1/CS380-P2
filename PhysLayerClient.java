//Michael Ly, CS380

import java.util.*;
import java.io.*;
import java.net.*;

public class PhysLayerClient {

    public static void main(String[] args)
    {
        try{
            //Connecting to server
            Socket socket = new Socket("codebank.xyz", 38002);
            if(socket.isConnected())
            {
                System.out.println("Connected to server");
            }

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            double avgBline, temp = 0;
            int[] serverA32 = new int[320];
            boolean[] highlow = new boolean[320];
            byte[] decBA = new byte[32];

            //getting preamble to determine baseline
            for(int i = 0; i < 64; i++)
            {
                double sig = (double) is.read();
                temp += sig;
            }

            avgBline = temp/64;
            System.out.printf("Baseline established from preamble: %.2f\n", avgBline);

            //getting signals from server
            for(int i = 0; i < serverA32.length; i++)
            {
                serverA32[i] = (int) is.read();
            }

            //comparing signals to baseline to determine NRZI
            for(int i = 0; i < serverA32.length; i++)
            {
                if(serverA32[i] <= avgBline)
                {
                    highlow[i] = false;
                }
                else
                {
                    highlow[i] = true;
                }
            }

            //Decoding NRZI to 0 and 1 bits
            String decodeNRZI = "";
            if(highlow[0] == false)
                decodeNRZI += 0;
            else if(highlow[0] == true)
                decodeNRZI += 1;

            for(int i = 1; i < highlow.length; i++)
            {
                if(highlow[i] == false)
                {
                    if(highlow[i-1] == false)
                        decodeNRZI += 0;
                    else
                        decodeNRZI += 1;
                }
                else if(highlow[i] == true)
                {
                    if(highlow[i-1] == false)
                        decodeNRZI += 1;
                    else
                        decodeNRZI += 0;
                }
            }

            //Decoding NRZI bits layered with 4b/5b to regular bytes
            System.out.print("Recieved 32 bytes: ");
            for(int i = 0; i < decodeNRZI.length()/10; i++)
            {
                String ubits = "", lbits = "";
                int decUBits, decLBits;

                ubits = decodeNRZI.substring(i*10, ((i*10)+5));
                lbits = decodeNRZI.substring((i*10)+5, ((i*10)+6+4));

                decUBits = switch5b4b(ubits);
                decLBits = switch5b4b(lbits);

                decUBits = decUBits << 4;
                decBA[i] = (byte) (decUBits  + decLBits);

                System.out.printf("%02X", decBA[i]);
            }

            System.out.println();
            os.write(decBA);

            int response = is.read();
            if(response == 1)
            {
                System.out.println("Response is good");
            }
            else if(response == 0)
            {
                System.out.println("Response is bad");
            }

            socket.close();
            if(socket.isClosed())
            {
                System.out.println("Disconnected from server");
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //4b/5b table
    public static int switch5b4b(String s) {

        switch (s) {
            case "11110":
                return 0;
            case "01001":
                return 1;
            case "10100":
                return 2;
            case "10101":
                return 3;
            case "01010":
                return 4;
            case "01011":
                return 5;
            case "01110":
                return 6;
            case "01111":
                return 7;
            case "10010":
                return 8;
            case "10011":
                return 9;
            case "10110":
                return 10;
            case "10111":
                return 11;
            case "11010":
                return 12;
            case "11011":
                return 13;
            case "11100":
                return 14;
            case "11101":
                return 15;
        }
        return -1;
    }
}
