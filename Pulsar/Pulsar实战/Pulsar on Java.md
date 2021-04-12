##Pulsar on Python

    import org.apache.pulsar.client.api.Message;
    import org.apache.pulsar.client.api.MessageId;
    import org.apache.pulsar.client.api.Reader;
    
    // Create a reader on a topic and for a specific message (and onward)
    Reader<byte[]> reader = pulsarClient.newReader()
        .topic("reader-api-test")
        .startMessageId(MessageId.earliest)
        .create();
    
    while (true) {
        Message message = reader.readNext();
    
        // Process the message
    }