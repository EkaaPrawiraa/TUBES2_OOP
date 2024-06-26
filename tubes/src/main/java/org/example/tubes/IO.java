package org.example.tubes;

import javafx.util.Pair;

import java.io.*;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IO {
    public static void save(String folderpath, GameState gamestate) {
        try {
            // Save GameState data to gamestate.txt
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(folderpath, "gamestate.txt").toString()))) {
                writer.write(String.valueOf(gamestate.getJumlahTurn()));
                writer.newLine();
                Map<Kartu, Integer> tokoItems = gamestate.getToko().getBarang();
                writer.write(String.valueOf(tokoItems.size()));
                writer.newLine();
                for (Kartu item : tokoItems.keySet()) {
                    writer.write(item.getName() + " " + tokoItems.get(item));
                    writer.newLine();
                }
            }

            // Save Player 1 data to player1.txt
            savePlayer(folderpath, gamestate.getPlayer1(), "player1.txt");

            // Save Player 2 data to player2.txt
            savePlayer(folderpath, gamestate.getPlayer2(), "player2.txt");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void savePlayer(String folderpath, Player player, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(folderpath, filename).toString()))) {
            writer.write(String.valueOf(player.getGulden()));
            writer.newLine();
            // Save active deck
            List<Kartu> activeDeck = player.getActiveDeck();
            writer.write(String.valueOf(activeDeck.size()));
            writer.newLine();
            //tulis sisa deck
            writer.write(String.valueOf(player.getKartuList().size()));
            writer.newLine();
            int i = 0;
            for (Kartu kartu : activeDeck) {
                if (!Objects.equals(kartu, null)){
                    String characterString = Character.toString((char) (i + 'A'));
                    characterString+="01";
                    writer.write(characterString+" "+ kartu.getName());
                    writer.newLine();
                }
                i++;
            }
            // Save Ladang (assuming Ladang has a method to retrieve its items in a serializable format)
            List<Mahluk> ladangItems = player.getLadang().getMahluk();
            int count = 0;
            for (Mahluk m : ladangItems) {
                if (!Objects.equals(m, null)) {
                    count++;
                }
            }
            writer.write(String.valueOf(count));
            writer.newLine();
            int index = 0;
            for (Mahluk item : ladangItems) {
                if (!Objects.equals(item, null)) {
                    writer.write(idxtostring(index)+ " "+ item.getName());
                    if (item instanceof Hewan) {
                        Hewan hewan = (Hewan) item;
                        writer.write(" " + hewan.getWeight() + " " + hewan.printeffect());
                    }else if (item instanceof Tanaman){
                        Tanaman tanaman = (Tanaman) item;
                        writer.write(" "+ tanaman.getAge()+" "+ tanaman.printeffect());
                    }
                    writer.newLine();
                }
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String idxtostring(int idx){
        int row = idx/5;
        int col = idx%5;
        return (char) (row + 'A') + String.format("%02d", col);
    }
    public static GameState load(String folderpath) throws IOException {
        GameState gamestate = new GameState();
        try (Stream<Path> paths = Files.walk(Paths.get(folderpath))) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
                    .toList();
            if (files.size()!=3){
                throw new Exception();
            }

            for (Path path: files){
                String fileName = path.getFileName().toString();
                if (fileName.equalsIgnoreCase("gamestate.txt")){
                    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()))) {
                        String line;
                        line = bufferedReader.readLine();
                        gamestate.jumlahTurn = Integer.parseInt(line);
                        line = bufferedReader.readLine();
                        int amount = Integer.parseInt(line);
                        Map<Kartu, Integer> toko= new HashMap<>();
                        for (int i = 0; i<amount;i++){
                            line = bufferedReader.readLine();
                            String[] storepair = line.split(" ");
                            toko.put(Utility.constructor(storepair[0]), Integer.parseInt(storepair[1]));
                        }
                        gamestate.setToko(new Store(toko));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else if (fileName.equalsIgnoreCase("player1.txt")){
                    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()))){
                        String line;
                        line = bufferedReader.readLine();
                        gamestate.player1.setGulden(Integer.parseInt(line));
                        line = bufferedReader.readLine();
                        //apain gitu
                        int len = Integer.parseInt(line);
                        System.out.println(len);
                        List<Kartu> listka = new ArrayList<>(6);
                        for (int i = 0; i<6;i++){
                            listka.add(null);
                        }
                        line = bufferedReader.readLine();
                        gamestate.player1.setKartuList(Main.generateAllCard());
                        while (gamestate.player1.getKartuList().size()>Integer.parseInt(line)) {
                            gamestate.player1.getKartuList().removeLast();
                        }
                        for (int i = 0; i< len;i++){
                            line = bufferedReader.readLine();
                            String[] parts = line.split(" ", 2);
                            char a = parts[0].charAt(0);
                            listka.set(a - 'A', Utility.constructor(parts[1]));
                        }
                        gamestate.player1.setActiveDeck(listka);
                        line = bufferedReader.readLine();
                        len = Integer.parseInt(line);

                        for (int i = 0; i <len; i++ ){
                            line = bufferedReader.readLine();
                            String[] parts = line.split(" ");
                            char a = parts[0].charAt(0);
                            String b = parts[0].substring(1);
                            Boolean word2 = false;
                            String mah = parts[1];
                            if (!isStringInteger(parts[2])){
                                mah= mah + " " +parts[2];
                                word2 = true;
                            }
                            Mahluk ma =(Mahluk) Utility.constructor(mah);
                            if (ma instanceof Hewan){
                                Hewan he = (Hewan) ma;
                                if (word2){
                                    he.setWeight(Integer.parseInt(parts[3]));
                                    for (int j = 0; j<Integer.parseInt(parts[4]);j++) {
                                        if (parts[j + 5].equalsIgnoreCase("ACCELERATE")) {
                                            he.accelerate++;
                                        } else if (parts[j + 5].equalsIgnoreCase("DELAY")) {
                                            he.delay++;
                                        } else if (parts[j + 5].equalsIgnoreCase("PROTECT")) {
                                            he.protect++;
                                        } else if (parts[j + 5].equalsIgnoreCase("TRAP")) {
                                            he.trap++;
                                        }
                                    }
                                }else {
                                    he.setWeight(Integer.parseInt(parts[2]));
                                    for (int j = 0; j<Integer.parseInt(parts[3]);j++) {
                                        if (parts[j + 4].equalsIgnoreCase("ACCELERATE")) {
                                            he.accelerate++;
                                        } else if (parts[j + 4].equalsIgnoreCase("DELAY")) {
                                            he.delay++;
                                        } else if (parts[j + 4].equalsIgnoreCase("PROTECT")) {
                                            he.protect++;
                                        } else if (parts[j + 4].equalsIgnoreCase("TRAP")) {
                                            he.trap++;
                                        }
                                    }
                                }
                            }else if (ma instanceof Tanaman){
                                Tanaman ta = (Tanaman) ma;
                                if (word2){
                                    ta.setAge(Integer.parseInt(parts[3]));
                                    for (int j = 0; j<Integer.parseInt(parts[4]);j++){
                                        if (parts[j+5].equalsIgnoreCase("ACCELERATE")){
                                            ta.accelerate++;
                                        }else if (parts[j+5].equalsIgnoreCase("DELAY")){
                                            ta.delay++;
                                        }else if (parts[j+5].equalsIgnoreCase("PROTECT")){
                                            ta.protect++;
                                        }else if (parts[j+5].equalsIgnoreCase("TRAP")){
                                            ta.trap++;
                                        }
                                    }
                                }else{
                                    ta.setAge(Integer.parseInt(parts[2]));
                                    for (int j = 0; j<Integer.parseInt(parts[3]);j++) {
                                        if (parts[j + 4].equalsIgnoreCase("ACCELERATE")) {
                                            ta.accelerate++;
                                        } else if (parts[j + 4].equalsIgnoreCase("DELAY")) {
                                            ta.delay++;
                                        } else if (parts[j + 4].equalsIgnoreCase("PROTECT")) {
                                            ta.protect++;
                                        } else if (parts[j + 4].equalsIgnoreCase("TRAP")) {
                                            ta.trap++;
                                        }
                                    }
                                }
                            }
                            int index = ((int)(a-'A')*5) + Integer.parseInt(b);
                            gamestate.player1.getLadang().addMahluk(ma, index);

                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else if (fileName.equalsIgnoreCase("player2.txt")){
                    try(BufferedReader bufferedReader = new BufferedReader(new FileReader(path.toFile()))){
                        String line;
                        line = bufferedReader.readLine();
                        gamestate.player2.setGulden(Integer.parseInt(line));
                        line = bufferedReader.readLine();
                        //apain gitu
                        int len = Integer.parseInt(line);
                        List<Kartu> listka = new ArrayList<>(len);
                        line = bufferedReader.readLine();
                        gamestate.player2.setKartuList(Main.generateAllCard());
                        while (gamestate.player2.getKartuList().size()>Integer.parseInt(line)) {
                            gamestate.player2.getKartuList().removeLast();
                        }
                        for (int i = 0; i< len;i++){
                            line = bufferedReader.readLine();
                            String[] parts = line.split(" ", 2);
                            char a = parts[0].charAt(0);
                            listka.set(a - 'A', Utility.constructor(parts[1]));
                        }
                        gamestate.player2.setActiveDeck(listka);
                        line = bufferedReader.readLine();
                        len = Integer.parseInt(line);

                        for (int i = 0; i <len; i++ ){
                            line = bufferedReader.readLine();
                            String[] parts = line.split(" ");
                            char a = parts[0].charAt(0);
                            String b = parts[0].substring(1);
                            Boolean word2 = false;
                            String mah = parts[1];
                            if (!isStringInteger(parts[2])){
                                mah = mah + " " + parts[2];
                                word2 = true;
                            }
                            Mahluk ma =(Mahluk) Utility.constructor(mah);
                            if (ma instanceof Hewan){
                                Hewan he = (Hewan) ma;
                                if (word2){
                                    he.setWeight(Integer.parseInt(parts[3]));
                                    for (int j = 0; j<Integer.parseInt(parts[4]);j++) {
                                        if (parts[j + 5].equalsIgnoreCase("ACCELERATE")) {
                                            he.accelerate++;
                                        } else if (parts[j + 5].equalsIgnoreCase("DELAY")) {
                                            he.delay++;
                                        } else if (parts[j + 5].equalsIgnoreCase("PROTECT")) {
                                            he.protect++;
                                        } else if (parts[j + 5].equalsIgnoreCase("TRAP")) {
                                            he.trap++;
                                        }
                                    }
                                }else {
                                    he.setWeight(Integer.parseInt(parts[2]));
                                    for (int j = 0; j<Integer.parseInt(parts[3]);j++) {
                                        if (parts[j + 4].equalsIgnoreCase("ACCELERATE")) {
                                            he.accelerate++;
                                        } else if (parts[j + 4].equalsIgnoreCase("DELAY")) {
                                            he.delay++;
                                        } else if (parts[j + 4].equalsIgnoreCase("PROTECT")) {
                                            he.protect++;
                                        } else if (parts[j + 4].equalsIgnoreCase("TRAP")) {
                                            he.trap++;
                                        }
                                    }
                                }
                            }else if (ma instanceof Tanaman){
                                Tanaman ta = (Tanaman) ma;
                                if (word2){
                                    ta.setAge(Integer.parseInt(parts[3]));
                                    for (int j = 0; j<Integer.parseInt(parts[4]);j++){
                                        if (parts[j+5].equalsIgnoreCase("ACCELERATE")){
                                            ta.accelerate++;
                                        }else if (parts[j+5].equalsIgnoreCase("DELAY")){
                                            ta.delay++;
                                        }else if (parts[j+5].equalsIgnoreCase("PROTECT")){
                                            ta.protect++;
                                        }else if (parts[j+5].equalsIgnoreCase("TRAP")){
                                            ta.trap++;
                                        }
                                    }
                                }else{
                                    ta.setAge(Integer.parseInt(parts[2]));
                                    for (int j = 0; j<Integer.parseInt(parts[3]);j++) {
                                        if (parts[j + 4].equalsIgnoreCase("ACCELERATE")) {
                                            ta.accelerate++;
                                        } else if (parts[j + 4].equalsIgnoreCase("DELAY")) {
                                            ta.delay++;
                                        } else if (parts[j + 4].equalsIgnoreCase("PROTECT")) {
                                            ta.protect++;
                                        } else if (parts[j + 4].equalsIgnoreCase("TRAP")) {
                                            ta.trap++;
                                        }
                                    }
                                }
                            }
                            int index = ((int)(a-'A')*5) + Integer.parseInt(b);
                            gamestate.player2.getLadang().addMahluk(ma, index);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return gamestate;
    }
    public static boolean isStringInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
//        try {
//            // Walk the directory tree and get a stream of all paths
////            Stream<Path> pathStream = Files.walk(Paths.get(folderpath), FileVisitOption.FOLLOW_LINKS);
//
//            // Filter the stream to include only regular files with .txt extension
//            pathStream.filter(Files::isRegularFile)
//                    .filter(path -> path.toString().toLowerCase().endsWith(".txt"))
//                    .forEach(path -> {
//                        // Filter by filename
//                        String fileName = path.getFileName().toString();
//                        if (fileName.equalsIgnoreCase("gamestate.txt") ||
//                                fileName.equalsIgnoreCase("player1.txt") ||
//                                fileName.equalsIgnoreCase("player2.txt")) {
//                            // Process each file path as needed
//                            System.out.println("File: " + path);
//                            // Read the contents of the file here
//                        }
//                    });
//        } catch (IOException e) {
//            System.err.println("Error accessing folder: " + folderpath);
//        }
}

