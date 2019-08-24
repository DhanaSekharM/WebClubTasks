package com.example.chatbot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.github.bassaer.chatmessageview.model.IChatUser;
import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.util.ChatBot;
import com.github.bassaer.chatmessageview.view.ChatView;

public class ChatviewActivity extends AppCompatActivity {

    private ChatView chatView;
    private IChatUser user, bot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatview);

        chatView = findViewById(R.id.chat_view);
        final String userId = "0", botId = "1";
        //add user Icon

        final String userName = "User", botName = "Bot";

        user = createUser(userId, userName);
        bot = createUser(botId, botName);

        setChatViewAttributes();

        chatView.setOnClickSendButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message sendMessage = new Message.Builder()
                        .setUser(user)
                        .setRight(true)
                        .setText(chatView.getInputText())
                        .hideIcon(true)
                        .build();
                makeRequest(chatView.getInputText());
                chatView.send(sendMessage);
                chatView.setInputText("");

            }
        });

    }

    private void makeRequest(String inputText) {

        Message recMessage = new Message.Builder()
                .setUser(bot)
                .setRight(false)
                .setText("Beep bop")
                .hideIcon(true)
                .build();
        chatView.receive(recMessage);

    }

    private void setChatViewAttributes() {
        chatView.setRightBubbleColor(Color.RED);
        chatView.setLeftBubbleColor(Color.WHITE);
        chatView.setBackgroundColor(ContextCompat.getColor(this, R.color.lightBlue));
        chatView.setRightMessageTextColor(Color.BLACK);
        chatView.setLeftMessageTextColor(Color.BLACK);
        chatView.setInputTextHint("");
        chatView.setMessageMarginTop(5);
        chatView.setMessageMarginBottom(5);
    }

    private IChatUser createUser(final String id, final String name) {
        return new IChatUser() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Bitmap getIcon() {
                return null;
            }

            @Override
            public void setIcon(Bitmap bitmap) {

            }
        };
    }
}
