package com.example.bletest.tool;

import java.util.HashMap;

public class SampleGattAttributes {

    private static HashMap<String, String> attributes = new HashMap<>();

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");

        // GATT Services
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "GenericAccess");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "GenericAttribute");

        // GATT Declarations
        attributes.put("00002800-0000-1000-8000-00805f9b34fb", "Primary Service");
        attributes.put("00002801-0000-1000-8000-00805f9b34fb", "Secondary Service");
        attributes.put("00002802-0000-1000-8000-00805f9b34fb", "Include");
        attributes.put("00002803-0000-1000-8000-00805f9b34fb", "Characteristic");

        // GATT Descriptors
        attributes.put("00002900-0000-1000-8000-00805f9b34fb", "Characteristic Extended Properties");
        attributes.put("00002901-0000-1000-8000-00805f9b34fb", "Characteristic User Description");
        attributes.put("00002902-0000-1000-8000-00805f9b34fb", "Client Characteristic Configuration");
        attributes.put("00002903-0000-1000-8000-00805f9b34fb", "Server Characteristic Configuration");
        attributes.put("00002904-0000-1000-8000-00805f9b34fb", "Characteristic Presentation Format");
        attributes.put("00002905-0000-1000-8000-00805f9b34fb", "Characteristic Aggregate Format");
        attributes.put("00002906-0000-1000-8000-00805f9b34fb", "Valid Range");
        attributes.put("00002907-0000-1000-8000-00805f9b34fb", "External Report Reference Descriptor");
        attributes.put("00002908-0000-1000-8000-00805f9b34fb", "Report Reference Descriptor");

        // GATT Characteristics
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "PPCP");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");

        // GATT Service UUIDs
        attributes.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert");
        attributes.put("00001803-0000-1000-8000-00805f9b34fb", "Link Loss");
        attributes.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power");
        attributes.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
        attributes.put("00001806-0000-1000-8000-00805f9b34fb", "Reference Time Update Service");
        attributes.put("00001807-0000-1000-8000-00805f9b34fb", "Next DST Change Service");
        attributes.put("00001808-0000-1000-8000-00805f9b34fb", "Glucose");
        attributes.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        attributes.put("0000180b-0000-1000-8000-00805f9b34fb", "Network Availability");
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate");
        attributes.put("0000180e-0000-1000-8000-00805f9b34fb", "Phone Alert Status Service");
        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        attributes.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure");
        attributes.put("00001811-0000-1000-8000-00805f9b34fb", "Alert Notification Service");
        attributes.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
        attributes.put("00001813-0000-1000-8000-00805f9b34fb", "Scan Parameters");
        attributes.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence");
        attributes.put("00001816-0000-1000-8000-00805f9b34fb", "Cycling Speed and Cadence");
        attributes.put("00001818-0000-1000-8000-00805f9b34fb", "Cycling Power");
        attributes.put("00001819-0000-1000-8000-00805f9b34fb", "Location and Navigation");

        // GATT Characteristic UUIDs
        attributes.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        attributes.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        attributes.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        attributes.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        attributes.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        attributes.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
        attributes.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
        attributes.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
        attributes.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
        attributes.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
        attributes.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
        attributes.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
        attributes.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
        attributes.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
        attributes.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
        attributes.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        attributes.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        attributes.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        attributes.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
        attributes.put("00002a21-0000-1000-8000-00805f9b34fb", "Measurement Interval");
        attributes.put("00002a22-0000-1000-8000-00805f9b34fb", "Boot Keyboard Input Report");
        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
        attributes.put("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
        attributes.put("00002a31-0000-1000-8000-00805f9b34fb", "Scan Refresh");
        attributes.put("00002a32-0000-1000-8000-00805f9b34fb", "Boot Keyboard Output Report");
        attributes.put("00002a33-0000-1000-8000-00805f9b34fb", "Boot Mouse Input Report");
        attributes.put("00002a34-0000-1000-8000-00805f9b34fb", "Glucose Measurement Context");
        attributes.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        attributes.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        attributes.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        attributes.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        attributes.put("00002a3e-0000-1000-8000-00805f9b34fb", "Network Availability");
        attributes.put("00002a3f-0000-1000-8000-00805f9b34fb", "Alert Status");
        attributes.put("00002a40-0000-1000-8000-00805f9b34fb", "Ringer Control Point");
        attributes.put("00002a41-0000-1000-8000-00805f9b34fb", "Ringer Setting");
        attributes.put("00002a42-0000-1000-8000-00805f9b34fb", "Alert Category ID Bit Mask");
        attributes.put("00002a43-0000-1000-8000-00805f9b34fb", "Alert Category ID");
        attributes.put("00002a44-0000-1000-8000-00805f9b34fb", "Alert Notification Control Point");
        attributes.put("00002a45-0000-1000-8000-00805f9b34fb", "Unread Alert Status");
        attributes.put("00002a46-0000-1000-8000-00805f9b34fb", "New Alert");
        attributes.put("00002a47-0000-1000-8000-00805f9b34fb", "Supported New Alert Category");
        attributes.put("00002a48-0000-1000-8000-00805f9b34fb", "Supported Unread Alert Category");
        attributes.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        attributes.put("00002a4a-0000-1000-8000-00805f9b34fb", "HID Information");
        attributes.put("00002a4b-0000-1000-8000-00805f9b34fb", "Report Map");
        attributes.put("00002a4c-0000-1000-8000-00805f9b34fb", "HID Control Point");
        attributes.put("00002a4d-0000-1000-8000-00805f9b34fb", "Report");
        attributes.put("00002a4e-0000-1000-8000-00805f9b34fb", "Protocol Mode");
        attributes.put("00002a4f-0000-1000-8000-00805f9b34fb", "Scan Interval Window");
        attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
        attributes.put("00002a51-0000-1000-8000-00805f9b34fb", "Glucose Feature");
        attributes.put("00002a52-0000-1000-8000-00805f9b34fb", "Record Access Control Point");
        attributes.put("00002a53-0000-1000-8000-00805f9b34fb", "RSC Measurement");
        attributes.put("00002a54-0000-1000-8000-00805f9b34fb", "RSC Feature");
        attributes.put("00002a55-0000-1000-8000-00805f9b34fb", "SC Control Point");
        attributes.put("00002a5b-0000-1000-8000-00805f9b34fb", "CSC Measurement");
        attributes.put("00002a5c-0000-1000-8000-00805f9b34fb", "CSC Feature");
        attributes.put("00002a5d-0000-1000-8000-00805f9b34fb", "Sensor Location");
        attributes.put("00002a63-0000-1000-8000-00805f9b34fb", "Cycling Power Measurement");
        attributes.put("00002a64-0000-1000-8000-00805f9b34fb", "Cycling Power Vector");
        attributes.put("00002a65-0000-1000-8000-00805f9b34fb", "Cycling Power Feature");
        attributes.put("00002a66-0000-1000-8000-00805f9b34fb", "Cycling Power Control Point");
        attributes.put("00002a67-0000-1000-8000-00805f9b34fb", "Location and Speed");
        attributes.put("00002a68-0000-1000-8000-00805f9b34fb", "Navigation");
        attributes.put("00002a69-0000-1000-8000-00805f9b34fb", "Position Quality");
        attributes.put("00002a6a-0000-1000-8000-00805f9b34fb", "LN Feature");
        attributes.put("00002a6b-0000-1000-8000-00805f9b34fb", "LN Control Point");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
