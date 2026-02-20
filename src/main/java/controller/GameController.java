package com.typearena.controller;

import com.typearena.model.GameMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class GameController {

    private final Map<String, List<GameMessage.PlayerInfo>> roomPlayers = new ConcurrentHashMap<>();

    // 15 KİŞİ İÇİN 15 ÖZEL E-SPOR RENGİ
    private final List<String> availableColors = Arrays.asList(
            "#6366f1", "#ef4444", "#10b981", "#f59e0b", "#ec4899",
            "#8b5cf6", "#06b6d4", "#84cc16", "#f97316", "#14b8a6",
            "#3b82f6", "#a855f7", "#eab308", "#f43f5e", "#0ea5e9"
    );

    // Fingertest Uzun-Kısa Zorlu Kelime Havuzu
    private final List<String> wordPool = Arrays.asList(
            "ve", "bir", "bu", "için", "gibi", "ile", "çok", "kadar", "daha", "zaman",
            "kendi", "büyük", "göre", "çünkü", "nasıl", "neden", "ancak", "şekilde", "sonra", "böyle",
            "bilgisayar", "teknoloji", "geliştirme", "mühendislik", "programlama", "algoritma",
            "yapay", "zeka", "kütüphane", "fonksiyon", "değişken", "optimizasyon", "senkronizasyon",
            "gerçekleştirmek", "değerlendirmek", "karşılaştırmak", "özelleştirmek", "sürdürülebilir",
            "kullanıcı", "deneyimi", "arayüz", "tasarım", "rekabet", "şampiyonluk", "istatistik"
    );

    @MessageMapping("/game/{roomCode}/join")
    @SendTo("/topic/room/{roomCode}")
    public GameMessage joinRoom(@DestinationVariable String roomCode, @Payload GameMessage message) {
        roomPlayers.putIfAbsent(roomCode, new CopyOnWriteArrayList<>());
        List<GameMessage.PlayerInfo> players = roomPlayers.get(roomCode);

        Optional<GameMessage.PlayerInfo> existingPlayer = players.stream()
                .filter(p -> p.getName().equals(message.getSender())).findFirst();

        if (existingPlayer.isEmpty() && players.size() < 15) { // 15 KİŞİ SINIRI
            GameMessage.PlayerInfo newPlayer = new GameMessage.PlayerInfo();
            newPlayer.setName(message.getSender());

            // Odaya ilk girişte boşta olan ilk rengi otomatik ata
            List<String> usedColors = players.stream().map(GameMessage.PlayerInfo::getColor).toList();
            String assignedColor = availableColors.stream().filter(c -> !usedColors.contains(c)).findFirst().orElse("#ffffff");
            newPlayer.setColor(assignedColor);
            newPlayer.setProgress(0);
            players.add(newPlayer);
        }

        message.setType(GameMessage.MessageType.ROOM_STATE);
        message.setPlayerList(players);
        return message;
    }

    // LOBİDE CANLI RENK DEĞİŞTİRME MANTIĞI
    @MessageMapping("/game/{roomCode}/changeColor")
    @SendTo("/topic/room/{roomCode}")
    public GameMessage changeColor(@DestinationVariable String roomCode, @Payload GameMessage message) {
        List<GameMessage.PlayerInfo> players = roomPlayers.get(roomCode);
        if (players != null) {
            // Renk başkası tarafından kapılmış mı?
            boolean isTaken = players.stream().anyMatch(p -> p.getColor().equals(message.getColor()));
            if (!isTaken) {
                for (GameMessage.PlayerInfo p : players) {
                    if (p.getName().equals(message.getSender())) {
                        p.setColor(message.getColor()); // Rengi onayla ve güncelle
                        break;
                    }
                }
            }
        }
        message.setType(GameMessage.MessageType.ROOM_STATE);
        message.setPlayerList(players);
        return message;
    }

    @MessageMapping("/game/{roomCode}/start")
    @SendTo("/topic/room/{roomCode}")
    public GameMessage startGame(@DestinationVariable String roomCode, @Payload GameMessage message) {
        StringBuilder wordsForThisRound = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 50; i++) wordsForThisRound.append(wordPool.get(random.nextInt(wordPool.size()))).append(" ");
        message.setContent(wordsForThisRound.toString().trim());
        return message;
    }

    @MessageMapping("/game/{roomCode}/progress")
    @SendTo("/topic/room/{roomCode}")
    public GameMessage updateProgress(@DestinationVariable String roomCode, @Payload GameMessage message) {
        List<GameMessage.PlayerInfo> players = roomPlayers.getOrDefault(roomCode, new ArrayList<>());
        for (GameMessage.PlayerInfo p : players) {
            if (p.getName().equals(message.getSender())) {
                p.setProgress(message.getProgress());
                p.setWpm(message.getWpm());
                break;
            }
        }
        return message;
    }
}