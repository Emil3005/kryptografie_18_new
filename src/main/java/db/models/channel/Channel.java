package db.models.channel;


import com.google.common.eventbus.EventBus;
import config.Config;
import db.dblogic.enums.DBService;
import db.models.message.MessageEventBus;
import db.models.user.User;

public class Channel {

    private final String name;
    private final User userA;
    private final User userB;
    private final EventBus eventBus = new EventBus();

    public Channel(String name, User userA, User userB) {
        this.name = name;
        this.userA = userA;
        this.userB = userB;
        eventBus.register(userA);
        eventBus.register(userB);
        if (Config.instance.hackedChannels.containsKey(this.name)) {
            eventBus.register(DBService.instance.getUser(Config.instance.hackedChannels.get(this.name)));
        }
    }

    public String getName() {
        return this.name;
    }

    public User getUserA() {
        return this.userA;
    }

    public User getUserB() {
        return this.userB;
    }

    public void send(MessageEventBus message) {
        eventBus.post(message);
    }

    public void intrude(User intruder) {
        eventBus.register(intruder);
        Config.instance.hackedChannels.put(this.name, intruder.getName());
    }

}
