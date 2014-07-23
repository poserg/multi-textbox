package com.github.poserg.client;

import com.github.poserg.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MultiTextBox implements EntryPoint {
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

    private final Messages messages = GWT.create(Messages.class);

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        final Button sendButton = new Button( messages.sendButton() );
        final TextBox nameField = new TextBox();
        nameField.setText( messages.nameField() );
        final Label errorLabel = new Label();
        final FlexTable fieldsTable = new FlexTable();
        fieldsTable.setText(0, 0, "Test");
        fieldsTable.setWidget(0, 1, new TextBox());
        fieldsTable.setText(1, 0, "Boolean");
        fieldsTable.setWidget(1, 1, new CheckBox());

        // We can add style names to widgets
        sendButton.addStyleName("sendButton");

        // Add the nameField and sendButton to the RootPanel
        // Use RootPanel.get() to get the entire body element
        RootPanel.get("nameFieldContainer").add(nameField);
        RootPanel.get("sendButtonContainer").add(sendButton);
        RootPanel.get("errorLabelContainer").add(errorLabel);
        RootPanel.get("phoneContainer").add(fieldsTable);
        buildPhoneWidget(fieldsTable);

        // Focus the cursor on the name field when the app loads
        nameField.setFocus(true);
        nameField.selectAll();

        // Create the popup dialog box
        final DialogBox dialogBox = new DialogBox();
        dialogBox.setText("Remote Procedure Call");
        dialogBox.setAnimationEnabled(true);
        final Button closeButton = new Button("Close");
        // We can set the id of a widget by accessing its Element
        closeButton.getElement().setId("closeButton");
        final Label textToServerLabel = new Label();
        final HTML serverResponseLabel = new HTML();
        VerticalPanel dialogVPanel = new VerticalPanel();
        dialogVPanel.addStyleName("dialogVPanel");
        dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
        dialogVPanel.add(textToServerLabel);
        dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
        dialogVPanel.add(serverResponseLabel);
        dialogVPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        dialogVPanel.add(closeButton);
        dialogBox.setWidget(dialogVPanel);

        // Add a handler to close the DialogBox
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                dialogBox.hide();
                sendButton.setEnabled(true);
                sendButton.setFocus(true);
            }
        });

        // Create a handler for the sendButton and nameField
        class MyHandler implements ClickHandler, KeyUpHandler {
            /**
             * Fired when the user clicks on the sendButton.
             */
            @Override
            public void onClick(final ClickEvent event) {
                sendNameToServer();
            }

            /**
             * Fired when the user types in the nameField.
             */
            @Override
            public void onKeyUp(final KeyUpEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    sendNameToServer();
                }
            }

            /**
             * Send the name from the nameField to the server and wait for a response.
             */
            private void sendNameToServer() {
                // First, we validate the input.
                errorLabel.setText("");
                String textToServer = nameField.getText();
                if (!FieldVerifier.isValidName(textToServer)) {
                    errorLabel.setText("Please enter at least four characters");
                    return;
                }

                // Then, we send the input to the server.
                sendButton.setEnabled(false);
                textToServerLabel.setText(textToServer);
                serverResponseLabel.setText("");
                greetingService.greetServer(textToServer, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(final Throwable caught) {
                        // Show the RPC error message to the user
                        dialogBox.setText("Remote Procedure Call - Failure");
                        serverResponseLabel.addStyleName("serverResponseLabelError");
                        serverResponseLabel.setHTML(SERVER_ERROR);
                        dialogBox.center();
                        closeButton.setFocus(true);
                    }

                    @Override
                    public void onSuccess(final String result) {
                        dialogBox.setText("Remote Procedure Call");
                        serverResponseLabel.removeStyleName("serverResponseLabelError");
                        serverResponseLabel.setHTML(result);
                        dialogBox.center();
                        closeButton.setFocus(true);
                    }
                });
            }
        }

        // Add a handler to send the name to the server
        MyHandler handler = new MyHandler();
        sendButton.addClickHandler(handler);
        nameField.addKeyUpHandler(handler);
    }

    private void buildPhoneWidget(final FlexTable flexTable) {
        int rowCount = flexTable.getRowCount();
        flexTable.setText(rowCount, 0, "Контактный телефон");

        final FlexTable phoneTable = new FlexTable();
        flexTable.setWidget(rowCount, 1, phoneTable);

        final Button addButton = new Button("Add");
        addPhoneField(phoneTable, addButton);

        addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                addPhoneField(phoneTable, addButton);
            }
        });

        Button printButton = new Button("Print");
        flexTable.setWidget(rowCount, 2, printButton);
        printButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < phoneTable.getRowCount(); i++) {
                    TextBox item = (TextBox) phoneTable.getWidget(i, 0);
                    sb.append(item.getText());
                    sb.append("\n");
                }

                Window.alert(sb.toString());
            }
        });
    }

    private void addPhoneField(final FlexTable flexTable, final Button addButton) {
        Button deleteButton = new Button("Delete");
        final TextBox phoneTextBox = new TextBox();
        int row = flexTable.getRowCount();
        deleteButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(final ClickEvent event) {
                int rowCount = flexTable.getRowCount();
                if (rowCount == 1) {
                    phoneTextBox.setText("");
                } else {
                    addButton.removeFromParent();
                    phoneTextBox.getElement().getParentNode().getParentNode().removeFromParent();
                    flexTable.setWidget(rowCount - 2, 2, addButton);
                }
            }
        });

        flexTable.setWidget(row, 0, phoneTextBox);
        flexTable.setWidget(row, 1, deleteButton);
        if (addButton.isAttached()) {
            addButton.removeFromParent();
        }
        flexTable.setWidget(row, 2, addButton);

        phoneTextBox.setFocus(true);
    }
}
