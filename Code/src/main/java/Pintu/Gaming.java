package Pintu;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

//拼图3*3

public class Gaming extends Application {
    long startTime;
    //pane排列
    BorderPane root = new BorderPane();
    GridPane gamePane = new GridPane();
    GridPane infoPane = new GridPane();


    Scene scene = new Scene(root);

    String inputPath = "/Images/MOS_shield.jpg";//初始化默认图片的地址

    Image white = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/white.jpg"))); //白色图片

    Image bg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Images/bg.jpg")), 263, 777, false, false); //背景图

    String rankPath = "rank.txt";//排行榜文件地址

    int whiteNum;//随机数组空缺的数字
    int[] randomNum = new int[9];//全局随机数

    ImageView[] imageViews = new ImageView[9];//图片组
    int flag = 0;//游戏状态
    int[] rank_array = new int[4];

    //infoPane组件
    Label hello = new Label();
    Date date = new Date();
    Button start = new Button("开始游戏");
    Button back = new Button("回到游戏");
    Button preview = new Button("查看大略缩图");
    ImageView ima = new ImageView();
    Label click = new Label("移动次数：0次");
    Label usedTime = new Label("使用时间: 00:00:00");
    int clickNum;

    //计时器
    pTimer timer = new pTimer(usedTime);

    //菜单组件
    MenuBar menuBar = new MenuBar();
    Menu game = new Menu("游戏");
    Menu help = new Menu("帮助");
    Menu about = new Menu("关于游戏");
    MenuItem input = new MenuItem("导入图片");
    MenuItem newGame = new MenuItem("新游戏");
    SeparatorMenuItem separate = new SeparatorMenuItem();
    MenuItem rank = new MenuItem("排行榜");
    MenuItem exit = new MenuItem("退出");
    MenuItem gameHelp = new MenuItem("游戏帮助");
    MenuItem gameInfo = new MenuItem("制作信息");


    //预览图窗口
    private class Preview extends Application {

        @Override
        public void start(Stage stage) {

            Image image = getImage();
            ImageView imageView = new ImageView(image);
            BorderPane borderPane = new BorderPane();
            Scene scene = new Scene(borderPane);
            imageView.setFitWidth(512);
            imageView.setPreserveRatio(true);

            //组装窗口
            borderPane.setCenter(imageView);
            stage.setScene(scene);
            stage.setTitle("预览拼图");
            stage.show();
        }


    }
    //获取图片
    private Image getImage() {
        Image image;
        if(inputPath.equals("/Images/MOS_shield.jpg")){
            image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(inputPath)));
        }else{
            File pic = new File(inputPath);
            image = new Image("file:///"+pic.getAbsolutePath());
        }
        return image;
    }

    //鼠标点击图片事件
    private class MyClick implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent e) {
            ImageView imageView = (ImageView) e.getSource();

            int w_r = GridPane.getRowIndex(imageViews[whiteNum]);//白色块的行号;
            int w_c = GridPane.getColumnIndex(imageViews[whiteNum]);//白色块的列号;
            int w = 3 * w_r + w_c;//白色块的序号
            double cellWidth = 258;
            double posX = imageView.getLayoutX() - imageViews[whiteNum].getLayoutX();
            double posY = imageView.getLayoutY() - imageViews[whiteNum].getLayoutY();
            //调整位置
            if ((posX == -cellWidth && posY == 0) || (posX == cellWidth && posY == 0) || (posX == 0 && posY == -cellWidth) || (posX == 0 && posY == cellWidth)) {
                swap(imageView, imageViews[whiteNum], w, GridPane.getRowIndex(imageView) * 3 + GridPane.getColumnIndex(imageView), flag);
            }
            if (isWin(imageViews)) win();
        }
    }

    //键盘控制移动
    private class MyKey implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent e) {
            KeyCode code = e.getCode();
            int w_r = GridPane.getRowIndex(imageViews[whiteNum]);//白色块的行号;
            int w_c = GridPane.getColumnIndex(imageViews[whiteNum]);//白色块的列号;
            int w = 3 * w_r + w_c;//白色块的序号
            if (code == KeyCode.W && w_r != 0) {
                swap(imageViews[whiteNum], imageViews[randomNum[3 * w_r + w_c - 3]], w, 3 * w_r + w_c - 3, flag);
            } else if (code == KeyCode.S && w_r != 2) {
                swap(imageViews[whiteNum], imageViews[randomNum[3 * w_r + w_c + 3]], w, 3 * w_r + w_c + 3, flag);
            } else if (code == KeyCode.A && w_c != 0) {
                swap(imageViews[whiteNum], imageViews[randomNum[w_c - 1 + 3 * w_r]], w, w_c - 1 + 3 * w_r, flag);
            } else if (code == KeyCode.D && w_c != 2) {
                swap(imageViews[whiteNum], imageViews[randomNum[w_c + 1 + 3 * w_r]], w, w_c + 1 + 3 * w_r, flag);
            }
            if (isWin(imageViews)) {
                win();
            }
        }
    }

    @Override
    public void start(Stage stage) {
        //对数组rank初始化
        for (int i = 0; i < 4; i++) {
            rank_array[i] = Integer.MAX_VALUE;
        }
        //程序运行时不允许鼠标点击拼图，点击开始游戏后才能运行
        gamePane.setMouseTransparent(true);
        start.setOnAction(actionEvent -> {
            flag = 1;
            timer.startTimer();
            gamePane.setMouseTransparent(false);
            startTime = System.currentTimeMillis();
            back.setDisable(false);
            hello.setText("开始拼图游戏吧~        ");
            start.setDisable(true);
            gamePane.requestFocus();
            clickNum = 0;
        });

        //回到游戏
        back.setOnMouseClicked(mouseEvent -> gamePane.requestFocus());

        //组装infoPane
        int hour = date.getHours();
        if (hour <= 6) {
            hello.setText("凌晨好!");
        } else if (hour <= 10) {
            hello.setText("早上好!");
        } else if (hour <= 14) {
            hello.setText("中午好!");
        } else if (hour <= 18) {
            hello.setText("下午好!");
        } else {
            hello.setText("晚上好!");
        }

        hello.setFont(Font.font("FangSong", FontWeight.MEDIUM, 14));
        hello.setText(hello.getText() + '\n' + "请点击下方按钮开始游戏~");
        hello.setTextFill(Color.PURPLE);

        click.setFont(Font.font("FangSong", FontWeight.NORMAL, 14));
        click.setAlignment(Pos.CENTER);

        usedTime.setFont(Font.font("FangSong", FontWeight.NORMAL, 16));
        usedTime.setTextFill(Color.BROWN);
        usedTime.setAlignment(Pos.CENTER);

        Image image = getImage();
        ima.setImage(image);
        ima.setFitWidth(150);
        ima.setFitHeight(150);

        back.setPrefHeight(40);
        back.setPrefWidth(150);
        back.setDisable(true);

        start.setPrefHeight(40);
        start.setPrefWidth(150);

        preview.setPrefHeight(40);
        preview.setPrefWidth(150);

        //设置背景图片
        infoPane.setBackground(new Background(new BackgroundImage(bg, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT)));
        infoPane.setVgap(30);
        infoPane.add(hello, 0, 2);
        infoPane.add(new Label("================="), 0, 3);
        infoPane.add(start, 0, 4);
        infoPane.add(preview, 0, 5);
        infoPane.add(back, 0, 6);
        infoPane.add(new Label("================="), 0, 7);
        infoPane.add(ima, 0, 8);
        infoPane.add(new Label("================="), 0, 9);
        infoPane.add(usedTime, 0, 10);
        infoPane.add(click, 0, 11);


        //实现导入图片or使用默认图片进行拼图
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导入图片");
        //给fileChooser添加过滤器
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"),
                new FileChooser.ExtensionFilter("GIF", "*.gif")
        );

        input.setOnAction(actionEvent -> {
            //抓取错误
            try {
                File file = fileChooser.showOpenDialog(stage);
                inputPath = file.getPath();
                //重置次数和时间
                click.setText("移动次数：0次");
                clickNum = 0;
                timer.clearTimer();
                //刷新
                showImage();
                ima.setImage(new Image("file:///" + inputPath));
                flag = 0;
                hello.setText("请点击下方按钮开始游戏~");
                gamePane.setMouseTransparent(true);
                start.setDisable(false);
                back.setDisable(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        //通过按钮点击显示预览拼图(内部类建立新窗口)

        preview.setOnAction(actionEvent -> {
            try {
                new Preview().start(new Stage());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        //新游戏选项
        newGame.setOnAction(actionEvent -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定要重新开始游戏吗?");
            Optional<ButtonType> buttonType = confirm.showAndWait();
            if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {
                //重置计数
                timer.clearTimer();
                click.setText("移动次数：0次");
                clickNum = 0;
                //刷新画面
                showImage();
                //重置游戏状态
                flag = 0;
                gamePane.setMouseTransparent(true);
                hello.setText("请点击下方按钮开始游戏~");
                start.setDisable(false);
                back.setDisable(true);
            }
        });

        //退出菜单
        exit.setOnAction(actionEvent -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确认要关闭游戏吗?");
            Optional<ButtonType> buttonType = confirm.showAndWait();
            if (buttonType.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE)) {

                System.exit(0);
            }
        });

        //排行榜功能
        rank.setOnAction(actionEvent -> {
            File rankFile = new File(rankPath);
            //文件不存在就创建
            if (!rankFile.exists()) {
                try {
                    rankFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                //读取文件内信息
                try {
                    FileReader reader = new FileReader(rankFile);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String line;
                    int index = 0;
                    while ((line = bufferedReader.readLine()) != null) {
                        rank_array[index] = Integer.parseInt(line);
                        index++;
                        if (index > 2) break;
                    }
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Arrays.sort(rank_array);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("排行榜");
            alert.setHeaderText("游戏记录");

            String[] rank = new String[3];
            for (int i = 0; i < 3; i++) {
                if (rank_array[i] == Integer.MAX_VALUE) {
                    rank[i] = "Unknown";
                } else {
                    rank[i] = rank_array[i] + "秒";
                }
            }
            alert.setContentText("第一名：" + rank[0] + '\n' + '\n' + "第二名：" + rank[1] + '\n' + '\n' + "第三名：" + rank[2]);
            alert.show();
        });

        //游戏帮助菜单
        gameHelp.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("帮助");
            alert.setHeaderText("游戏帮助");
            //文本块
            alert.setContentText("1.这是一个拼图游戏！通过移动空白块使拼图完整即可获得胜利~\n" +
                                 "\n" +
                                 "2.可以使用鼠标点击空白块附近（上下左右）的方块或使用键盘WSAD（英文输入法下）进行操作~\n" +
                                 "\n" +
                                 "3.点击开始游戏后才能对方块进行操作！计时、计步从点击按钮后启动~\n" +
                                 "\n" +
                                 "4.您可以自行导入图片，请使用菜单【游戏】->【导入图片】功能！\n" +
                                 "\n" +
                                 "5.您如果创造出杰出的记录，会保存在排行榜中！\n" +
                                 "\n" +
                                 "6.如果开始游戏后出现键盘或鼠标不能操作的情况，请点击“回到游戏”按钮。");
            alert.show();
        });

        gameInfo.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("游戏信息");
            alert.setHeaderText("游戏制作信息");
            alert.setContentText("1004215219归远楠 完成于2023/1/1 14:00\n" +
                                 "\n" +
                                 "新年快乐！");
            alert.show();
        });


        //显示随机分割后的拼图
        showImage();

        //组装菜单
        game.getItems().addAll(newGame, input, rank, separate, exit);
        help.getItems().add(gameHelp);
        about.getItems().add(gameInfo);
        menuBar.getMenus().addAll(game, help, about);

        //组装stage
        root.setTop(menuBar);
        root.setCenter(gamePane);
        root.setLeft(infoPane);
        infoPane.setPadding(new Insets(0, 50, 0, 50));

        gamePane.setOnKeyPressed(new MyKey());
        gamePane.setVgap(2);
        gamePane.setHgap(2);
        stage.setScene(scene);
        stage.setTitle("拼图");
        stage.show();

    }

    //显示随机分割后的拼图
    public void showImage() {

        int[] r_game = r_series(); //生成随机数组
        ImageView[] g_image = cutImage(); //切割导入的图片or默认图片
        for (int i = 0; i < 8; i++) {//行标号
            gamePane.add(g_image[r_game[i]], i % 3, i / 3);
        }
        gamePane.add(g_image[whiteNum], 2, 2);
    }

    //对预览图进行切割
    public ImageView[] cutImage() {
        Image image;
        if(inputPath.equals("/Images/MOS_shield.jpg")){
            image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(inputPath)),768,768,false,false);
        }else{
            File pic = new File(inputPath);
            image = new Image("file:///"+pic.getAbsolutePath(),768,768,false,false);
        }
        double eachWeight = image.getWidth() / 3;
        //对图片组初始化
        for (int i = 0; i < 9; i++) {
            imageViews[i] = new ImageView(image);
            imageViews[i].setOnMouseClicked(new MyClick());
        }

        for (int i = 0, index = 0; i < 3; i++) {//行标号
            for (int j = 0; j < 3; j++, index++) {//列标号
                imageViews[index].setViewport(new Rectangle2D(eachWeight * j, eachWeight * i, eachWeight, eachWeight));
            }
        }
        //设定空白格
        imageViews[whiteNum] = new ImageView(white);
        imageViews[whiteNum].setViewport(new Rectangle2D(0, 0, eachWeight, eachWeight));

        return imageViews;
    }

    //生成含8个不重复的数、且逆序数为偶数的数列,并找出数组中没有的数(0~8)
    public int[] r_series() {
        int[] r = new int[8];
        Random random = new Random();
        while (!isEven(r)) {
            for (int i = 0; i < 8; i++) {
                r[i] = random.nextInt(9);
                //对已经生成的数进行不重复验证
                //不允许 1 2 2 或者 1 2 3 1
                for (int j = 0; j < i; j++) {
                    if (r[i] == r[j]) i--;
                }
            }
        }

        //找到数组中没有的数(0~8)
        for (int num = 0; num <= 8; num++) {
            int flag = 0;
            for (int j : r) {
                if (j == num) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                whiteNum = num;
                break;
            }
        }
        System.arraycopy(r, 0, randomNum, 0, 8);
        randomNum[8] = whiteNum;
        return r;
    }

    //判断逆序数是否为偶数
    public boolean isEven(int[] r) {
        int inverse_n = 0;
        for (int i = 0; i < 7; i++) {
            for (int j = i; j < 8; j++) {
                if (r[i] > r[j]) inverse_n++;
            }
        }
        return inverse_n % 2 == 0 && inverse_n != 0;
    }


    //实现交换，交换后同时交换下标数据
    public void swap(ImageView imv1, ImageView imv2, int i1, int i2, int game_f) {
        if (game_f == 1) {
            //获取第一幅图的坐标
            int r1 = GridPane.getRowIndex(imv1); //行
            int c1 = GridPane.getColumnIndex(imv1); //列
            //获取第二幅图的坐标
            int r2 = GridPane.getRowIndex(imv2);
            int c2 = GridPane.getColumnIndex(imv2);
            //交换
            GridPane.setRowIndex(imv1, r2);
            GridPane.setColumnIndex(imv1, c2);
            GridPane.setRowIndex(imv2, r1);
            GridPane.setColumnIndex(imv2, c1);
            clickNum++;
            click.setText("移动次数：" + clickNum + "次");
            //交换后同时交换下标数据
            int temp = randomNum[i1];
            randomNum[i1] = randomNum[i2];
            randomNum[i2] = temp;
        }
    }

    //判断拼图是否完成
    //归位：如imv[7] 应该在A的位置,即3*行号(1)+列号
    // 0 1 2
    // 3 4 5
    // 6 7
    public boolean isWin(ImageView[] imv) {
        for (int i = 0; i < 9; i++) {
            if (i != 3 * GridPane.getRowIndex(imv[i]) + GridPane.getColumnIndex(imv[i]))
                return false;
        }
        return true;
    }

    //拼图完成画面
    public void win() {
        long endTime = System.currentTimeMillis();
        long usedTime = (endTime - startTime) / 1000;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        //清除游戏状态
        flag = 0;
        timer.clearTimer();
        alert.setTitle("完成");
        alert.setHeaderText("恭喜您完成拼图！");
        alert.setContentText("共用时" + usedTime + "秒，移动" + clickNum + "次~");
        //如果超过排行榜内的记录则保存
        int rank_flag = 0;
        System.out.println(Arrays.toString(rank_array));
        for (int i = 0; i <= 2; i++) {
            if (usedTime < rank_array[i]) {
                rank_flag = 1;
                break;
            }
        }
        if (rank_flag == 1) {
            rank_array[3] = (int) usedTime;
            Arrays.sort(rank_array);//6
            System.out.println(Arrays.toString(rank_array));
            alert.setContentText(alert.getContentText() + '\n' + '\n' + "您创造的记录成功进入排行榜中");
            //覆盖rank.txt
            try {
                FileWriter fileWriter = new FileWriter(rankPath, false);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(String.valueOf(rank_array[0]) + '\n' + rank_array[1] + '\n' + rank_array[2]);
                bufferedWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        alert.show();
    }


    public static void main(String[] args) {
        Application.launch(args);
    }
}
