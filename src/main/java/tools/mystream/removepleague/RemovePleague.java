package tools.mystream.removepleague;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.user.update.GenericUserPresenceEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class RemovePleague extends ListenerAdapter {
    private static final String GAME = "league of legends";
    private static final boolean IS_KICKING = false;
    private static final boolean IS_MESSAGING = false;
    private static Map<Member, Instant> users = new HashMap<>();

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDA jda = JDABuilder.createLight(args[0], GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS)
                .enableCache(CacheFlag.ACTIVITY).build();
        jda.addEventListener(new RemovePleague());

        jda.awaitReady();
        System.out.println("Ready!");
        for (Guild guild : jda.getGuilds()) {
            System.out.println(guild.getName());
            guild.loadMembers().onSuccess(guildMembers -> {
                System.out.println("Loaded " + guildMembers.size() + " members");
                int i = 0;
                for (Member guildMember : guildMembers) {
                    System.out.println(i++ + ". " + guildMember.getUser().getName());
                    for (Activity activity : guildMember.getActivities()) {
                        if (activity.getName().toLowerCase().contains(GAME)) {
                            System.out.println("Adding " + guildMember.getUser().getName() + "#" + guildMember.getUser().getDiscriminator());
                            users.put(guildMember, Instant.now());
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onGenericUserPresence(@NotNull GenericUserPresenceEvent event) {
        for (Activity activity : event.getMember().getActivities()) {
            if (activity.getName().contains(GAME)) {
                System.out.println("Adding " + event.getMember().getUser().getName() + "#" + event.getMember().getUser().getDiscriminator());
                users.put(event.getMember(), Instant.now());
            }
        }

        for (Member member : users.keySet()) {
            for (Activity activity : member.getActivities()) {
                if (activity.getName().contains(GAME)) {
                    if (users.get(member).plus(30, ChronoUnit.MINUTES).isAfter(Instant.now())) {
                        // ban em
                        System.out.println("Kicking " + member.getUser().getName() + "#" + member.getUser().getDiscriminator());
                        // send them a message
                        if (IS_MESSAGING) {
                            member.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage("You have been kicked from the server for playing " + GAME + " for 30 minutes.").queue());
                        }

                        if (IS_KICKING) {
                            member.kick("Playing " + GAME + " for 30 minutes.").queue();
                        }
                    }
                }
            }
        }
    }
}
