package com.project.chatapp.model;

import com.google.firebase.database.PropertyName;

public class ChatMessage {
    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        LOCATION,
        VOICE
    }

    private String id;
    private String fromId;
    private String toId;
    private String content;
    private String timeStamp;
    private boolean isSender;
    private MessageType messageType;
    private boolean deletedForMe;

    public boolean isDeletedForMe() {
        return deletedForMe;
    }

    public void setDeletedForMe(boolean deletedForMe) {
        this.deletedForMe = deletedForMe;
    }

    @PropertyName("type")
    public void setType(String type) {
        try {
            this.messageType = MessageType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            this.messageType = MessageType.TEXT;
        }
    }

    @PropertyName("type")
    public String getType() {
        return messageType.name().toLowerCase();
    }

    public ChatMessage() {
    }

    public ChatMessage(String fromId, String toId, String content, String timeStamp) {
        this.fromId = fromId;
        this.toId = toId;
        this.content = content;
        this.timeStamp = timeStamp;
        this.messageType = detectMessageType(content);
    }

    private MessageType detectMessageType(String content) {
        if (content == null) return MessageType.TEXT;

        String lowerContent = content.toLowerCase();

        if (content.startsWith("voice:")) {
            return MessageType.VOICE;
        }
        else if (content.startsWith("location:")) {
            return MessageType.LOCATION;
        }
        else if (lowerContent.contains("cloudinary.com")) {
            if (lowerContent.contains("/video/")) {
                return MessageType.VIDEO;
            } else if (lowerContent.contains("/image/")) {
                return MessageType.IMAGE;
            }
        }
        // Kiểm tra các định dạng file khác
        else if (isImageFile(lowerContent)) {
            return MessageType.IMAGE;
        } else if (isVideoFile(lowerContent)) {
            return MessageType.VIDEO;
        }

        return MessageType.TEXT;
    }

    private boolean isImageFile(String content) {
        return content.endsWith(".jpg") ||
                content.endsWith(".jpeg") ||
                content.endsWith(".png") ||
                content.endsWith(".gif") ||
                content.endsWith(".bmp") ||
                content.endsWith(".webp");
    }

    private boolean isVideoFile(String content) {
        return content.endsWith(".mp4") ||
                content.endsWith(".avi") ||
                content.endsWith(".mov") ||
                content.endsWith(".wmv") ||
                content.endsWith(".flv") ||
                content.endsWith(".webm");
    }

    public String getDisplayContent() {
        switch (messageType) {
            case VOICE:
                return content.startsWith("voice:") ? content.substring(6) : content;
            case LOCATION:
                return "Vị trí được chia sẻ";
            case IMAGE:
                return "Đã gửi một hình ảnh";
            case VIDEO:
                return "Đã gửi một video";
            default:
                return content;
        }
    }

    public boolean isMediaMessage() {
        return messageType == MessageType.IMAGE ||
                messageType == MessageType.VIDEO ||
                messageType == MessageType.VOICE;
    }

    public String getMediaUrl() {
        if (messageType == MessageType.VOICE && content.startsWith("voice:")) {
            return content.substring(6);
        }
        return content;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.messageType = detectMessageType(content);
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public static ChatMessage createVoiceMessage(String fromId, String toId, String voiceUrl, String timeStamp) {
        ChatMessage message = new ChatMessage(fromId, toId, "voice:" + voiceUrl, timeStamp);
        return message;
    }

    public static ChatMessage createLocationMessage(String fromId, String toId, double latitude, double longitude, String timeStamp) {
        String locationContent = "location:" + latitude + "," + longitude;
        ChatMessage message = new ChatMessage(fromId, toId, locationContent, timeStamp);
        return message;
    }

    public double[] getLocationCoordinates() {
        if (messageType == MessageType.LOCATION && content.startsWith("location:")) {
            try {
                String coords = content.substring(9); // Remove "location:"
                String[] parts = coords.split(",");
                if (parts.length == 2) {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    return new double[]{lat, lng};
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}