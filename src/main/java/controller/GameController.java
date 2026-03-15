package com.typearena.controller;

import com.typearena.model.GameMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*")
public class GameController {

    private final Map<String, List<GameMessage.PlayerInfo>> roomPlayers = new ConcurrentHashMap<>();
    private final List<String> availableColors = Arrays.asList("#6366f1", "#ef4444", "#10b981", "#f59e0b", "#ec4899", "#8b5cf6", "#06b6d4", "#84cc16", "#f97316", "#14b8a6", "#3b82f6", "#a855f7", "#eab308", "#f43f5e", "#0ea5e9");

    private final List<LeaderboardEntry> globalLeaderboard = new CopyOnWriteArrayList<>();

    // NİHAİ: 10FASTFINGERS KELİME HAVUZU (Benzersiz ve Günlük Dilden)
    private final List<String> wordPool = Arrays.asList(
            "ve", "bir", "bu", "da", "için", "ile", "çok", "gibi", "daha", "kadar", "kendi", "en", "sonra", "başka", "çünkü", "böyle", "sadece", "neden", "ancak", "zaman", "insan", "hayat", "gün", "yıl", "şey", "iyi", "yeni", "yok", "var", "hiç", "evet", "hayır", "tamam", "büyük", "küçük", "ilk", "son", "önce", "şimdi", "bugün", "burada", "orada", "içeri", "dışarı", "kim", "ne", "nasıl", "hangi", "nerede", "niye", "ben", "sen", "o", "biz", "siz", "onlar", "benim", "senin", "onun", "bizim", "sizin", "onların", "bana", "sana", "ona", "bize", "size", "onlara", "beni", "seni", "onu", "bizi", "sizi", "onları", "kız", "erkek", "kadın", "adam", "çocuk", "baba", "anne", "kardeş", "dost", "arkadaş", "ev", "okul", "iş", "yol", "yer", "su", "para", "göz", "el", "yüz", "baş", "ses", "kitap", "kelime", "isim", "soru", "cevap", "durum", "olay", "gece", "sabah", "akşam", "saat", "dakika", "hafta", "ay", "doğru", "yanlış", "güzel", "kötü", "zor", "kolay", "hızlı", "yavaş", "uzun", "kısa", "eski", "genç", "yaşlı", "tek", "aynı", "farklı", "bütün", "tüm", "her", "bazı", "biraz", "fazla", "az", "tam", "yarım", "çeyrek", "birkaç", "herkes", "hiçbiri", "kimse", "yapmak", "gelmek", "gitmek", "çalışmak", "okumak", "yazmak", "bilmek", "söylemek", "düşünmek", "anlamak", "görmek", "bakmak", "almak", "vermek", "bulmak", "kalmak", "kullanmak", "çıkmak", "durmak", "yaşamak", "sevmek", "inanmak", "beklemek", "aramak", "sormak", "cevaplamak", "başlamak", "bitirmek", "oynamak", "kazanmak", "kaybetmek", "koşmak", "yürümek", "oturmak", "kalkmak", "uyumak", "uyanmak", "yemek", "içmek", "izlemek", "dinlemek", "konuşmak", "susmak", "gülmek", "ağlamak", "duygu", "akıl", "fikir", "bilgi", "haber", "oyun", "şarkı", "film", "resim", "sanat", "bilim", "tarih", "doğa", "deniz", "orman", "güneş", "yıldız", "gökyüzü", "rüzgar", "yağmur", "sebep", "sonuç", "amaç", "araç", "değer", "fiyat", "miktar", "cümle", "sayfa", "kalem", "defter", "çanta", "araba", "uçak", "gemi", "tren", "otobüs", "bisiklet", "doktor", "öğretmen", "öğrenci", "mühendis", "işçi", "patron", "polis", "asker", "müdür", "işlem", "sistem", "yöntem", "kural", "kanun", "görev", "başarı", "kazanç", "kayıp", "tehlike", "güven", "şüphe", "hareket", "macera", "hikaye", "tiyatro", "müzik", "spor", "takım", "şampiyon", "ödül", "ceza", "mahkeme", "kanıt", "belge", "imza", "mektup", "mesaj", "selam", "teşekkür", "özür", "rica", "istek", "emir", "yasak", "izin", "onay", "kabul", "şart", "koşul", "seçenek", "tercih", "karar", "hedef", "yaptım", "geldi", "gidiyor", "çalışacak", "okur", "yazdı", "bilecek", "söyledi", "düşünüyor", "anladı", "gördü", "baktı", "alacak", "veriyor", "buldu", "kaldı", "çıktı", "durdu", "yaşıyor", "sevdi", "bekliyor", "aradı"
    );

    @GetMapping("/api/leaderboard")
    @ResponseBody
    public List<LeaderboardEntry> getLeaderboard() {
        return globalLeaderboard.stream()
                .sorted((a, b) -> Integer.compare(b.getWpm(), a.getWpm()))
                .limit(5)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/leaderboard")
    @ResponseBody
    public void addScore(@RequestBody LeaderboardEntry entry) {
        globalLeaderboard.add(entry);
    }

    @MessageMapping("/game/{roomCode}/join")
    @SendTo("/topic/room/{roomCode}")
    public GameMessage joinRoom(@DestinationVariable String roomCode, @Payload GameMessage message) {
        roomPlayers.putIfAbsent(roomCode, new CopyOnWriteArrayList<>());
        List<GameMessage.PlayerInfo> players = roomPlayers.get(roomCode);
        Optional<GameMessage.PlayerInfo> existingPlayer = players.stream().filter(p -> p.getName().equals(message.getSender())).findFirst();

        if (existingPlayer.isEmpty() && players.size() < 15) {
            GameMessage.PlayerInfo newPlayer = new GameMessage.PlayerInfo();
            newPlayer.setName(message.getSender());
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

    @MessageMapping("/game/{roomCode}/changeColor")
    @SendTo("/topic/room/{roomCode}")
    public GameMessage changeColor(@DestinationVariable String roomCode, @Payload GameMessage message) {
        List<GameMessage.PlayerInfo> players = roomPlayers.get(roomCode);
        if (players != null) {
            boolean isTaken = players.stream().anyMatch(p -> p.getColor().equals(message.getColor()));
            if (!isTaken) {
                for (GameMessage.PlayerInfo p : players) {
                    if (p.getName().equals(message.getSender())) {
                        p.setColor(message.getColor());
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
        // NİHAİ: SHUFFLE (KARIŞTIRMA) MANTIĞI
        List<String> shuffledList = new ArrayList<>(wordPool);
        Collections.shuffle(shuffledList);
        String words = String.join(" ", shuffledList.subList(0, 200));

        message.setContent(message.getContent() + "|" + words);
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

    public static class LeaderboardEntry {
        private String name;
        private int wpm;
        private int acc;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getWpm() { return wpm; }
        public void setWpm(int wpm) { this.wpm = wpm; }
        public int getAcc() { return acc; }
        public void setAcc(int acc) { this.acc = acc; }
    }
}