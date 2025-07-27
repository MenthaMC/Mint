package dev.bacteriawa.mint.utils;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MessageData {
    public static final String CHAT = "chat";
    public static final String ACTIONBAR = "actionbar";
    public static final String TITLE = "title";
    public static final String SUBTITLE = "subtitle";
    public static final String SOUND = "sound";

    private final String chat;
    private final String actionBar;
    private final TitleData title;
    private final SoundData sound;
    private final String pattern;

    public MessageData(String chat, String actionBar, TitleData title, SoundData sound, String pattern) {
        this.chat = chat;
        this.actionBar = actionBar;
        this.title = title;
        this.sound = sound;
        this.pattern = pattern;
    }

    public MessageData(String pattern) {
        this(parse(pattern));
    }

    public MessageData(MessageData copy) {
        this(copy.chat, copy.actionBar, copy.title, copy.sound, copy.pattern);
    }

    public ParsedMessageData parsed(TagResolver... params) {
        Component chatComponent = chat != null ? MiniMessage.miniMessage().deserialize(chat, params) : null;
        Component actionBarComponent = actionBar != null ? MiniMessage.miniMessage().deserialize(actionBar, params) : null;
        TitleData.ParsedTitleData titleData = title != null ? title.parsed(params) : null;
        return new ParsedMessageData(chatComponent, actionBarComponent, titleData, sound);
    }

    public String getChat() {
        return chat;
    }

    public TitleData getTitle() {
        return title;
    }

    public SoundData getSound() {
        return sound;
    }

    public String getPattern() {
        return pattern;
    }

    public String getString() {
        if (pattern != null) {
            return pattern;
        }

        StringBuilder sb = new StringBuilder();
        
        if (chat != null) {
            sb.append(chat);
        }
        
        if (actionBar != null) {
            sb.append("<").append(ACTIONBAR).append(">");
            sb.append(actionBar);
        }
        
        if (title != null) {
            boolean titleTime = true;
            if (title.getTitle() != null) {
                sb.append("<").append(TITLE);
                if (titleTime) {
                    titleTime = false;
                    if (title.getTimes() != null) {
                        sb.append(":").append(title.getTimes().getString());
                    }
                }
                sb.append(">");
                sb.append(title.getTitle());
            }
            
            if (title.getSubTitle() != null) {
                sb.append("<").append(SUBTITLE);
                if (titleTime) {
                    titleTime = false;
                    if (title.getTimes() != null) {
                        sb.append(":").append(title.getTimes().getString());
                    }
                }
                sb.append(">");
                sb.append(title.getSubTitle());
            }
        }
        
        if (sound != null) {
            sb.append("<").append(SOUND).append(":").append(sound.getString()).append(">");
        }
        
        return sb.toString();
    }

    public static MessageData parse(String string) {
        MessageDataBuilder builder = new MessageDataBuilder(string);
        MessageType messageType = MessageType.CHAT;
        boolean handled = false;

        StringBuilder buffer = new StringBuilder();
        StringBuilder tagBuffer = new StringBuilder();

        int pointer = 0;
        int length = string.length();
        while (pointer < length) {
            char ch = string.charAt(pointer++);
            boolean escaped = false;
            
            if (ch == '\\' && pointer < length) {
                buffer.append(ch);
                ch = string.charAt(pointer++);
                escaped = true;
            }
            
            if (!escaped && ch == '>') {
                String tag = tagBuffer.toString();
                String[] split = tag.split(":", 2);
                String main = split[0].toLowerCase();
                String off = split.length > 1 ? split[1] : "";

                MessageType type = messageType;
                boolean matches = false;
                
                switch (main) {
                    case CHAT:
                        messageType = MessageType.CHAT;
                        matches = true;
                        break;
                    case ACTIONBAR:
                        messageType = MessageType.ACTIONBAR;
                        matches = true;
                        break;
                    case TITLE:
                        messageType = MessageType.TITLE;
                        parseTitleTimes(builder, off);
                        matches = true;
                        break;
                    case SUBTITLE:
                        messageType = MessageType.SUBTITLE;
                        parseTitleTimes(builder, off);
                        matches = true;
                        break;
                    case SOUND:
                        String[] soundSplit = off.split(":");
                        if (soundSplit.length < 2) {
                            System.err.println("Failed to parse sound: At least namespace + key arguments required");
                        } else {
                            String namespace = soundSplit[0];
                            String key = soundSplit[1];
                            Sound.Source source = null;
                            Float volume = null;
                            Float pitch = null;
                            Long seed = null;
                            
                            if (soundSplit.length > 2) {
                                try {
                                    source = Sound.Source.valueOf(soundSplit[2].toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    // Ignore invalid source
                                }
                            }
                            
                            if (soundSplit.length > 3) {
                                try {
                                    volume = Float.parseFloat(soundSplit[3]);
                                } catch (NumberFormatException e) {
                                    // Ignore invalid volume
                                }
                            }
                            
                            if (soundSplit.length > 4) {
                                try {
                                    pitch = Float.parseFloat(soundSplit[4]);
                                } catch (NumberFormatException e) {
                                    // Ignore invalid pitch
                                }
                            }
                            
                            if (soundSplit.length > 5) {
                                try {
                                    seed = Long.parseLong(soundSplit[5]);
                                } catch (NumberFormatException e) {
                                    // Ignore invalid seed
                                }
                            }
                            
                            builder.setSound(new SoundData(namespace, key, source, volume, pitch, seed));
                            messageType = MessageType.SOUND;
                            matches = true;
                        }
                        break;
                }
                
                if (matches) {
                    String messageStr = buffer.toString();
                    if (messageStr.length() >= tagBuffer.length() + 1) {
                        messageStr = messageStr.substring(0, messageStr.length() - (tagBuffer.length() + 1));
                    } else {
                        messageStr = "";
                    }
                    
                    if (handled || !messageStr.isEmpty()) {
                        setMessage(type, builder, messageStr);
                    }
                    buffer.setLength(0);
                    tagBuffer.setLength(0);
                    handled = true;
                    continue;
                }
            }
            
            buffer.append(ch);
            tagBuffer.append(ch);
            if (!escaped && ch == '<') {
                tagBuffer.setLength(0);
            }
        }

        if (handled || buffer.length() > 0) {
            setMessage(messageType, builder, buffer.toString());
        }
        
        return builder.build();
    }

    private static void parseTitleTimes(MessageDataBuilder builder, String string) {
        if (string.isEmpty()) {
            return;
        }
        
        String[] split = string.split(":");
        if (split.length > 0) {
            if (split.length != 3) {
                System.err.println("Failed to parse title times: Exactly 3 arguments required; Your input is '" + string + "'");
            } else {
                List<Duration> durations = new ArrayList<>();
                for (String s : split) {
                    Long ticks = null;
                    try {
                        ticks = Long.parseLong(s);
                    } catch (NumberFormatException ignored) {
                    }
                    
                    if (ticks != null) {
                        durations.add(Duration.ofMillis(ticks * 50));
                    } else {
                        try {
                            durations.add(Duration.parse(s));
                        } catch (Exception e) {
                            System.err.println("Failed to parse duration: " + s);
                            durations.add(Duration.ZERO);
                        }
                    }
                }
                
                if (durations.size() >= 3) {
                    builder.setTitleTimes(new TitleData.Times(durations.get(0), durations.get(1), durations.get(2)));
                }
            }
        }
    }

    private static void setMessage(MessageType messageType, MessageDataBuilder builder, String message) {
        switch (messageType) {
            case CHAT:
                builder.setChat(message);
                break;
            case ACTIONBAR:
                builder.setActionBar(message);
                break;
            case TITLE:
                builder.setTitle(message);
                break;
            case SUBTITLE:
                builder.setSubTitle(message);
                break;
            case SOUND:
                // Do nothing for sound
                break;
        }
    }

    public static class MessageDataBuilder {
        private final String pattern;
        private String chat;
        private String actionBar;
        private String title;
        private String subTitle;
        private TitleData.Times titleTimes;
        private SoundData sound;

        public MessageDataBuilder(String pattern) {
            this.pattern = pattern;
        }

        public void setChat(String chat) {
            this.chat = chat;
        }

        public void setActionBar(String actionBar) {
            this.actionBar = actionBar;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public void setTitleTimes(TitleData.Times titleTimes) {
            this.titleTimes = titleTimes;
        }

        public void setSound(SoundData sound) {
            this.sound = sound;
        }

        public MessageData build() {
            TitleData titleData = null;
            if (title != null || subTitle != null || titleTimes != null) {
                titleData = new TitleData(title, subTitle, titleTimes);
            }
            return new MessageData(chat, actionBar, titleData, sound, pattern);
        }
    }

    enum MessageType {
        CHAT,
        ACTIONBAR,
        TITLE,
        SUBTITLE,
        SOUND
    }

    // Inner classes that would need to be defined separately in Java
    public static class ParsedMessageData {
        private final Component chat;
        private final Component actionBar;
        private final TitleData.ParsedTitleData title;
        private final SoundData sound;

        public ParsedMessageData(Component chat, Component actionBar, TitleData.ParsedTitleData title, SoundData sound) {
            this.chat = chat;
            this.actionBar = actionBar;
            this.title = title;
            this.sound = sound;
        }

        public Component getChat() {
            return chat;
        }

        public Component getActionBar() {
            return actionBar;
        }

        public TitleData.ParsedTitleData getTitle() {
            return title;
        }

        public SoundData getSound() {
            return sound;
        }
    }

    public static class TitleData {
        private final String title;
        private final String subTitle;
        private final Times times;

        public TitleData(String title, String subTitle, Times times) {
            this.title = title;
            this.subTitle = subTitle;
            this.times = times;
        }

        public String getTitle() {
            return title;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public Times getTimes() {
            return times;
        }

        public ParsedTitleData parsed(TagResolver... params) {
            Component titleComponent = title != null ? MiniMessage.miniMessage().deserialize(title, params) : null;
            Component subTitleComponent = subTitle != null ? MiniMessage.miniMessage().deserialize(subTitle, params) : null;
            return new ParsedTitleData(titleComponent, subTitleComponent, times);
        }

        public static class Times {
            private final Duration fadeIn;
            private final Duration stay;
            private final Duration fadeOut;

            public Times(Duration fadeIn, Duration stay, Duration fadeOut) {
                this.fadeIn = fadeIn;
                this.stay = stay;
                this.fadeOut = fadeOut;
            }

            public Duration getFadeIn() {
                return fadeIn;
            }

            public Duration getStay() {
                return stay;
            }

            public Duration getFadeOut() {
                return fadeOut;
            }

            public String getString() {
                // Convert durations to string representation
                return String.format("%s:%s:%s",
                        formatDuration(fadeIn),
                        formatDuration(stay),
                        formatDuration(fadeOut));
            }

            private String formatDuration(Duration duration) {
                long millis = duration.toMillis();
                if (millis % 1000 == 0) {
                    return (millis / 1000) + "s";
                } else {
                    return millis + "ms";
                }
            }
        }

        public static class ParsedTitleData {
            private final Component title;
            private final Component subTitle;
            private final Times times;

            public ParsedTitleData(Component title, Component subTitle, Times times) {
                this.title = title;
                this.subTitle = subTitle;
                this.times = times;
            }

            public Component getTitle() {
                return title;
            }

            public Component getSubTitle() {
                return subTitle;
            }

            public Times getTimes() {
                return times;
            }
        }
    }

    public static class SoundData {
        private final String namespace;
        private final String key;
        private final Sound.Source source;
        private final Float volume;
        private final Float pitch;
        private final Long seed;

        public SoundData(String namespace, String key, Sound.Source source, Float volume, Float pitch, Long seed) {
            this.namespace = namespace;
            this.key = key;
            this.source = source;
            this.volume = volume;
            this.pitch = pitch;
            this.seed = seed;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getKey() {
            return key;
        }

        public Sound.Source getSource() {
            return source;
        }

        public Float getVolume() {
            return volume;
        }

        public Float getPitch() {
            return pitch;
        }

        public Long getSeed() {
            return seed;
        }

        public String getString() {
            StringBuilder sb = new StringBuilder();
            sb.append(namespace).append(":").append(key);
            
            if (source != null) {
                sb.append(":").append(source.name().toLowerCase());
            }
            
            if (volume != null) {
                sb.append(":").append(volume);
            }
            
            if (pitch != null) {
                sb.append(":").append(pitch);
            }
            
            if (seed != null) {
                sb.append(":").append(seed);
            }
            
            return sb.toString();
        }
    }
}