package com.appcoins.communication.processor;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;

import com.appcoins.communication.ProcessedValueReturner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class MessageSenderTest {

    public static final String SENDER_URI = "appcoins://testing";
    public static final String PACKAGE_NAME = "package_name";
    private ProcessedValueReturner messageSender;
    private Context context;

    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
        messageSender = new ProcessedValueReturner(context, SENDER_URI);
    }

    @Test
    public void sendMessage() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
        Mockito.doNothing()
                .when(context)
                .startActivity(argumentCaptor.capture());

        Intent response = new Intent("");
        messageSender.returnValue(PACKAGE_NAME, 1L, response);

        Intent intent = argumentCaptor.getValue();

        assertEquals(1L, intent.getLongExtra("REQUEST_CODE", -1));
        assertEquals(response, intent.getParcelableExtra("RETURN_VALUE"));
        assertEquals(PACKAGE_NAME, intent.getPackage());
    }
}