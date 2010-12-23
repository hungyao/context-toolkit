/***************************************************************************/
/* COACH: Component Based Open Source Architecture for                     */
/*        Distributed Telecom Applications                                 */
/* See:   http://www.objectweb.org/                                        */
/*                                                                         */
/* Copyright (C) 2003 Lucent Technologies Nederland BV                     */
/*                    Bell Labs Advanced Technologies - EMEA               */
/*                                                                         */
/* Initial developer(s): Harold Batteram                                   */
/*                                                                         */
/* This library is free software; you can redistribute it and/or           */
/* modify it under the terms of the GNU Lesser General Public              */
/* License as published by the Free Software Foundation; either            */
/* version 2.1 of the License, or (at your option) any later version.      */
/*                                                                         */
/* This library is distributed in the hope that it will be useful,         */
/* but WITHOUT ANY WARRANTY; without even the implied warranty of          */
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU        */
/* Lesser General Public License for more details.                         */
/*                                                                         */
/* You should have received a copy of the GNU Lesser General Public        */
/* License along with this library; if not, write to the Free Software     */
/* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA */
/***************************************************************************/
package org.coach.tracing.service.ntp;

import java.net.*;

/**
 * This class encapsulates the header of a NtpDatagram. See rfc2030 for more 
 * details.
 */
public class NtpHeader  {
    /**
   * The default header data for a client datagram. Version=3, Mode=client.
   */
    public static final byte[] defaultHeaderData = { (byte)0x1B, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    /**
   * The default header for a client datagram. This is a wrapper around
   * 'defaultHeaderData'
   */
    public static final NtpHeader defaultHeader = new NtpHeader(defaultHeaderData);
    private byte[] data;
    /**
   * Reference identifier is InetAddress.
   */
    private static final byte RI_IP_ADDRESS = 0;
    /**
   * Reference identifier is String.
   */
    private static final byte RI_CODE = 1;
    /**
   * Reference identifier is 4 byte array.
   */
    private static final byte RI_OTHER = 2;
    private int mp(byte b) {
        int bb = b;
        return (bb < 0) ? 256 + bb : bb;
    }

    /**
   * Construct a NtpHeader from a 16 byte array.
   */
    public NtpHeader(byte[] data) {
        this.data = data;
    }

    /**
   * Gets the 16 byte array constituting the header.
   */
    public byte[] getData() {
        return data;
    }

    public int getLeapYearIndicator() {
        return (int)((data[0] & 0xc0) >>> 6);
    }

    public int getVersionNumber() {
        return (int)((data[0] & 0x38) >>> 3);
    }

    public int getMode() {
        return (int)(data[0] & 0x07);
    }

    public int getStratum() {
        return (int)data[1];
    }

    public int getPollInterval() {
        return (int)Math.round(Math.pow(2, data[2]));
    }

    /**
   * Get precision in milliseconds.
   */
    public double getPrecision() {
        return 1000 * Math.pow(2, data[3]);
    }

    /**
   * Get root delay in milliseconds.
   */
    public double getRootDelay() {
        int temp = 0;
        temp = 256 * (256 * (256 * data[4] + data[5]) + data[6]) + data[7];
        return 1000 * (((double)temp) / 0x10000);
    }

    /**
   * Get root dispersion in milliseconds.
   */
    public double getRootDispersion() {
        long temp = 0;
        temp = 256 * (256 * (256 * data[8] + data[9]) + data[10]) + data[11];
        return 1000 * (((double)temp) / 0x10000);
    }

    /**
   * Gets the type of the reference identifier.
   */
    private int getReferenceIdentifierType() {

        if (getMode() == NtpInfo.MODE_CLIENT) {
            return RI_OTHER;
        } else if (getStratum() < 2) {
            return RI_CODE;
        } else if (getVersionNumber() <= 3) {
            return RI_IP_ADDRESS;
        } else {
            return RI_OTHER;
        }
    }

    private InetAddress getReferenceAddress() throws IllegalArgumentException, UnknownHostException {

        if (getReferenceIdentifierType() != RI_IP_ADDRESS) {
            throw new IllegalArgumentException();
        }
        String temp = "" + mp(data[12]) + "." + mp(data[13]) + "." + mp(data[14]) + "." + mp(data[15]);
        return InetAddress.getByName(temp);
    }

    private String getReferenceCode() throws IllegalArgumentException {

        if (getReferenceIdentifierType() != RI_CODE) {
            throw new IllegalArgumentException();
        }
        int codeLength = 0;
        int index = 12;
        boolean zeroFound = false;

        while ((!zeroFound) && (index <= 15)) {

            if (data[index] == 0) {
                zeroFound = true;
            } else {
                index++;
                codeLength++;
            }
        }
        return new String(data, 12, codeLength);
    }

    private byte[] getReferenceData() {
        byte[] temp = new byte[4];
        temp[0] = data[12];
        temp[1] = data[13];
        temp[2] = data[14];
        temp[3] = data[15];
        return temp;
    }

    /**
   * Gets the  reference identifier as an object. It can be either a
   * String, a InetAddress or a 4 byte array. Use 'instanceof' to find out what 
   * the true class is.
   */
    public Object getReferenceIdentifier() {

        if (getReferenceIdentifierType() == RI_IP_ADDRESS) {
            try {
                return getReferenceAddress();
            } catch (Exception e) {
                return getReferenceData();
            }
        } else if (getReferenceIdentifierType() == RI_CODE) {
            return getReferenceCode();
        } else {
            return getReferenceData();
        }
    }

    public String toString() {
        String s = "Leap year indicator : " + getLeapYearIndicator() + "\n" + "Version number : " + getVersionNumber() + "\n" + "Mode : " + getMode() + "\n" + "Stratum : " + getStratum() + "\n" + "Poll interval : " + getPollInterval() + " s\n" + "Precision : " + getPrecision() + " ms\n" + "Root delay : " + getRootDelay() + " ms\n" + "Root dispersion : " + getRootDispersion() + " ms\n";
        Object o = getReferenceIdentifier();

        if (o instanceof InetAddress) {
            s = s + "Reference address : " + (InetAddress)o;
        } else if (o instanceof String) {
            s = s + "Reference code : " + (String)o;
        } else {
            byte[] temp = (byte[])o;
            s = s + "Reference data : " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3];
        }
        return s;
    }
}
