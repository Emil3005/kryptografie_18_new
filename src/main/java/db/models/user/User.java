/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.models.user;

import com.google.common.eventbus.Subscribe;
import db.models.message.MessageEventBus;
import db.models.user.userTypes.IUserType;
import db.models.user.userTypes.Intruder;
import db.models.user.userTypes.UserType;

public class User {
    private String name;
    private IUserType type;

    public User(String name, String input){
        this.name = name;
        if (input.equals("intruder")) {
            this.type = new Intruder();
        } else{
            this.type = new UserType(this);
        }
    }

    @Subscribe
    public void receiveMessage(MessageEventBus message){
        type.receiveMessage(message);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type.toString();
    }
}
