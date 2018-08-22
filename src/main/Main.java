package main;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import methods.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.security.cert.Extension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Main extends Application {

    private String imgPath;

    private Tab tab0;
    private BorderPane pane0 = new BorderPane();
    private ScrollPane selectedImageView;
    private Button inputButton;

    private Tab tab1;
    private BorderPane pane1 = new BorderPane();
    private ScrollPane ditheringImageView = new ScrollPane(new Group(new ImageView()));
    private Label ditheringLabel;

    private Tab tab2;
    private BorderPane pane2 = new BorderPane();
    private ScrollPane morphologyImageView = new ScrollPane();
    private RadioButton selectBinario = new RadioButton();
    private RadioButton selectMonocromatico = new RadioButton();

    private Tab tab3;
    private BorderPane pane3 = new BorderPane();
    private Label regionGrowingLabel = new Label();
    private ScrollPane regionGrowingImageView = new ScrollPane();
    private Slider rangeSlider = new Slider(1, 255, 100);

    private TabPane tabPane;

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        setupTabs();

        inputButton.setOnAction(e -> {
            var fileChooser = new FileChooser();
            fileChooser.setTitle("Selecione uma Imagem");
            var file = fileChooser.showOpenDialog(primaryStage);
            try {
                imgPath = file.toURI().toString();
                selectedImageView.setContent(newImageView());
                ditheringImageView.setContent(newImageView());
                morphologyImageView.setContent(newImageView());
                regionGrowingImageView.setContent(newImageView());

                var imagePane3 = ((ImageView)((Group)regionGrowingImageView.getContent()).getChildren().get(0));
                imagePane3.setOnMouseClicked(me -> {
                    var x = (int)me.getX();
                    var y = (int)me.getY();
                    System.out.println("["+x+", "+y+"]");
                    callSegmentation(y, x, (int)rangeSlider.getValue()).mostrar();
                });

                setDisabledTabs(false);
            } catch (Exception e1) { System.out.println(file.getAbsolutePath()); }
        });

        var scene = new Scene(tabPane, Screen.getPrimary().getBounds().getWidth() * 0.6, Screen.getPrimary().getBounds().getHeight());
        primaryStage.setTitle("Processamento de imagem");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(300);
        primaryStage.setMinWidth(300);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void setupTabs() {
        tab0 = new Tab("Imagem", pane0);
        tab1 = new Tab("Dithering", pane1);
        tab2 = new Tab("Morfologia", pane2);
        tab3 = new Tab("Crescimento de Região", pane3);

        setDisabledTabs(true, tab0);

        // Tabs general configuration.
        tab0.setClosable(false);
        tab1.setClosable(false);
        tab2.setClosable(false);
        tab3.setClosable(false);

        // TabPanel configuration.
        tabPane = new TabPane();
        tabPane.getTabs().add(tab0);
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        tabPane.getTabs().add(tab3);

        // tab0 configurations!
        selectedImageView = new ScrollPane(new ImageView());
        inputButton = new Button("Selecionar Imagem");
        pane0.setCenter(selectedImageView);
        pane0.setBottom(inputButton);
        BorderPane.setMargin(selectedImageView, new Insets(10,10,0,10));
        BorderPane.setMargin(inputButton, new Insets(10,10,10,10));
        BorderPane.setAlignment(inputButton, Pos.CENTER);

        // tab1 configuration.
        setupTab1();

        // tab2 configuration.
        setupTab2();

        // tab3 configuration.
        setupTab3();
    }

    private void setupTab1() {
        var ditheringBottomBox = new HBox();
        ditheringBottomBox.setSpacing(5);
        ditheringBottomBox.setAlignment(Pos.CENTER);
        var buttons = new ArrayList<PButton>() {{
            add(new PButton("Limiar Simples", MethodType.limiarSimples));
            add(new PButton("Modulação Aleatória", MethodType.moduladoAleatorio));
            add(new PButton("Periódico por Dispersão", MethodType.periodicoDisperso));
            add(new PButton("Periódico por Aglomeração", MethodType.periodicoAglomerado));
            add(new PButton("Aperiódico por Dispersão", MethodType.aperiodicoDisperso));

        }};
        buttons.forEach(button -> {
            button.setOnAction(e -> {
                var bufferedImg = callMethod(button.methodType, ditheringLabel).getBuffer();
                ditheringImageView.setContent(new Group(new ImageView(SwingFXUtils.toFXImage(bufferedImg, null))));
            });
        });
        ditheringBottomBox.getChildren().addAll(buttons);
        pane1.setBottom(ditheringBottomBox);
        buttons.forEach(button -> { button.setMaxWidth(Integer.MAX_VALUE); });
        pane1.setCenter(ditheringImageView);
        ditheringLabel = new Label("Escolha um método a partir dos botões inferiores");
        ditheringLabel.autosize();
        pane1.setTop(ditheringLabel);

        var ditheringRightBox = newImageControls(ditheringImageView);
        pane1.setRight(ditheringRightBox);
        BorderPane.setAlignment(ditheringLabel, Pos.CENTER);
        BorderPane.setMargin(ditheringLabel, new Insets(10,10,0,10));
        BorderPane.setMargin(ditheringBottomBox, new Insets(10,10,10,10));
        BorderPane.setMargin(ditheringImageView, new Insets(10,10,0,10));
        BorderPane.setMargin(ditheringRightBox, new Insets(10,10,10,0));
    }

    private void setupTab2() {
        var buttonsCommon = new ArrayList<PButton>() {{
            add(new PButton("Erodir", MethodType.erosao));
            add(new PButton("Dilatar", MethodType.dilatacao));
            add(new PButton("Abertura", MethodType.abertura));
            add(new PButton("Fechamento", MethodType.fechamento));
        }};
        var buttonsMonocromatico = new ArrayList<PButton>() {{
            add(new PButton("Gradiente", MethodType.gradiente));
            add(new PButton("Smoothing", MethodType.smoothing));
        }};
        var buttonsBinario = new ArrayList<PButton>() {{
            add(new PButton("Borda Externa", MethodType.bordaExterna));
            add(new PButton("Borda Interna", MethodType.bordaInterna));
        }};
        var buttons = new ArrayList<PButton>();
        buttons.addAll(buttonsCommon);
        buttons.addAll(buttonsBinario);
        buttons.addAll(buttonsMonocromatico);

        buttons.forEach(button -> {
            button.setOnAction(e -> {
                var bufferedImg = callMethod(button.methodType, new Label()).getBuffer();
                morphologyImageView.setContent(new Group(new ImageView(SwingFXUtils.toFXImage(bufferedImg, null))));
            });
        });

        selectBinario.selectedProperty().addListener(e -> {
            if (selectBinario.isSelected()) {
                buttonsMonocromatico.forEach(btn -> { btn.setDisable(true); });
                buttonsBinario.forEach(btn -> { btn.setDisable(false); });
            } else {
                buttonsMonocromatico.forEach(btn -> { btn.setDisable(false); });
                buttonsBinario.forEach(btn -> { btn.setDisable(true); });
            }
        });
        selectBinario.setText("Imagem Binária");
        selectBinario.setSelected(true);
        selectMonocromatico.setText("Imagem Monocromática");
        var buttonsGroup = new ToggleGroup();
        selectBinario.setToggleGroup(buttonsGroup);
        selectMonocromatico.setToggleGroup(buttonsGroup);

        var topBox = new HBox(selectBinario, selectMonocromatico);
        topBox.setAlignment(Pos.CENTER);
        topBox.setSpacing(25);
        pane2.setTop(topBox);
        BorderPane.setMargin(topBox, new Insets(10,10,10,10));

        var bottomBox = new HBox();
        buttons.forEach(btn -> { bottomBox.getChildren().add(btn); });

        bottomBox.setSpacing(5);
        bottomBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(bottomBox, new Insets(10,10,10,10));
        pane2.setBottom(bottomBox);

        BorderPane.setMargin(morphologyImageView, new Insets(0,10,0,10));
        pane2.setCenter(morphologyImageView);

        var rightBox = newImageControls(morphologyImageView);
        pane2.setRight(rightBox);
        BorderPane.setMargin(rightBox, new Insets(0,10,10,0));
    }

    private void setupTab3() {
        regionGrowingLabel.setText("Clique em uma área da imagem para destacar uma região. Use o Slider para ajustar a sensibilidade");
        pane3.setTop(regionGrowingLabel);
        pane3.setCenter(regionGrowingImageView);
        var rightBox = newImageControls(regionGrowingImageView);
        rightBox.getChildren().remove(2);
        pane3.setRight(rightBox);
        rangeSlider.setShowTickLabels(true);
        pane3.setBottom(rangeSlider);

        BorderPane.setMargin(regionGrowingLabel, new Insets(10,10,10,10));
        BorderPane.setMargin(regionGrowingImageView, new Insets(0,0,10,10));
        BorderPane.setAlignment(regionGrowingLabel, Pos.CENTER);
        BorderPane.setMargin(rightBox, new Insets(0,10,10,10));
        BorderPane.setMargin(rangeSlider, new Insets(0,10,10,10));
    }

    private Group newImageView() {
        return new Group(new ImageView(new Image(imgPath)));
    }

    private VBox newImageControls(ScrollPane nodeControlled) {
        var moreZoomBtn = new Button();
        var lessZoomBtn = new Button();
        var undoChanges = new Button();
        var exportImage = new Button();

        moreZoomBtn.setMaxWidth(Integer.MAX_VALUE);
        lessZoomBtn.setMaxWidth(Integer.MAX_VALUE);
        undoChanges.setMaxWidth(Integer.MAX_VALUE);
        exportImage.setMaxWidth(Integer.MAX_VALUE);
        moreZoomBtn.setGraphic(new ImageView("zoomIn.png"));
        lessZoomBtn.setGraphic(new ImageView("zoomOut.png"));
        undoChanges.setGraphic(new ImageView("undo.png"));
        exportImage.setGraphic(new ImageView("export.png"));
        var zoomBtns = new VBox(moreZoomBtn, lessZoomBtn, undoChanges, exportImage);

        exportImage.setOnAction(e -> {
            var imageView = (ImageView)(((Group)nodeControlled.getContent()).getChildren().get(0));
            var image = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar Imagem");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    ImageIO.write(image, "jpg", file);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        moreZoomBtn.setOnAction(e -> {
            try {
                var imageView = ((Parent)nodeControlled.getContent()).getChildrenUnmodifiable().get(0);
                imageView.setScaleX(0.125 + imageView.getScaleX());
                imageView.setScaleY(0.125 + imageView.getScaleY());
            } catch (Exception ee) {
                var imageView = nodeControlled.getContent();
                imageView.setScaleX(0.125 + imageView.getScaleX());
                imageView.setScaleY(0.125 + imageView.getScaleY());
            }
        });
        lessZoomBtn.setOnAction(e -> {
            try {
                var imageView = ((Parent)nodeControlled.getContent()).getChildrenUnmodifiable().get(0);
                if (imageView.getScaleY() - 0.125 >= 0 && imageView.getScaleX() - 0.125 >= 0) {
                    imageView.setScaleX(-0.125 + imageView.getScaleX());
                    imageView.setScaleY(-0.125 + imageView.getScaleY());
                }
            } catch (Exception ee) {
                var imageView = nodeControlled.getContent();
                if (imageView.getScaleY() - 0.125 >= 0 && imageView.getScaleX() - 0.125 >= 0) {
                    imageView.setScaleX(-0.125 + imageView.getScaleX());
                    imageView.setScaleY(-0.125 + imageView.getScaleY());
                }
            }
        });
        undoChanges.setOnAction(e -> {
            nodeControlled.setContent(newImageView());
        });

        return zoomBtns;
    }

    private void setDisabledTabs(boolean bolean, Tab except) {
        tab0.setDisable(bolean);
        tab1.setDisable(bolean);
        tab2.setDisable(bolean);
        tab3.setDisable(bolean);

        except.setDisable(!bolean);
    }

    private Imagem callMethod(MethodType type, Label label) {
        int N;
        int[][] B = {};
        label.setText(type.description(""));
        var image = new Imagem((new Imagem(SwingFXUtils.fromFXImage(new Image(imgPath), null))).toGray());
        switch (type) {
            case limiarSimples:
                return Dithering.limiarSimples(image);
            case moduladoAleatorio:
                return Dithering.modulacaoAleatoria(image);
            case aperiodicoDisperso:
                return Dithering.aperiodicoDispersao(image);
            case periodicoDisperso:
                N = getMatrizDimension( new ArrayList<Integer>(){{ add(2); add(4); add(8); add(16); }} );
                label.setText(type.description(" " + N + "X" + N));
                return Dithering.periodicoDispersao(image, N);
            case periodicoAglomerado:
                N = getMatrizDimension( new ArrayList<Integer>(){{ add(2); add(4); add(8); }} );
                label.setText(type.description(" " + N + "X" + N));
                return Dithering.periodicoAglomeracao(image, N);
            case erosao:
                B = getStructElement();
                if (selectBinario.isSelected()) {
                    return MathematicalMorphology.Binary.erode(image, B);
                } return MathematicalMorphology.Monochrome.erode(image, B);
            case dilatacao:
                B = getStructElement();
                if (selectBinario.isSelected()) {
                    return MathematicalMorphology.Binary.dilate(image, B);
                } return MathematicalMorphology.Monochrome.dilate(image, B);
            case abertura:
                B = getStructElement();
                if (selectBinario.isSelected()) {
                    return MathematicalMorphology.Binary.opening(image, B);
                } return MathematicalMorphology.Monochrome.opening(image, B);
            case fechamento:
                B = getStructElement();
                if (selectBinario.isSelected()) {
                    return MathematicalMorphology.Binary.closing(image, B);
                } return MathematicalMorphology.Monochrome.closing(image, B);
            case bordaExterna:
                B = getStructElement();
                return MathematicalMorphology.Binary.outerEdge(image, B);
            case bordaInterna:
                B = getStructElement();
                return MathematicalMorphology.Binary.innerEdge(image, B);
            case smoothing:
                B = getStructElement();
                return MathematicalMorphology.Monochrome.smoothing(image, B);
            case gradiente:
                B = getStructElement();
                return MathematicalMorphology.Monochrome.gradient(image, B);
        }

        return null;
    }

    private Imagem callSegmentation(int y, int x, int range) {
        var image = (new Imagem(SwingFXUtils.fromFXImage(new Image(imgPath), null)));
        var pixel = new Pixel(y, x, image.getPixel(y, x, 0), image.getPixel(y,x,1), image.getPixel(y,x,2));
        return Segmentation.regionGrowing(image, pixel, range);
    }

    private int[][] getStructElement() {
        int[][] B = {};
        TextInputDialog dialog = new TextInputDialog("1,1,1 ; 1,1,1 ; 1,1,1");
        dialog.setTitle("Elementro Estrutrante");
        dialog.setHeaderText("Informe o elemento estrutante (Matriz), separando as linhas por ponto e vírgula. Informe todas as linhas com a mesma quantidade de números.");
        var result = dialog.showAndWait().get();
        if (result != null) {
            var rows = result.split(";");
            B = new int[rows.length][rows[0].split(",").length];
            for (int i = 0; i < rows.length; i++) {
                var column = rows[i].split(",");
                for (int j = 0; j < column.length; j++) {
                    B[i][j] = Integer.parseInt(column[j].trim());
                }
            }
            System.out.println(Arrays.deepToString(B));
        }
        return B;
    }

    private int getMatrizDimension(int min, int max) {
        var choices = new ArrayList<Integer>();
        IntStream.range(min, max).forEach(n -> { choices.add(n); });
        var dialog = new ChoiceDialog<Integer>(2, choices);
        dialog.setTitle("Dimensão da Matriz");
        dialog.setHeaderText("Escolha a dimensão da matriz de dithering");
        dialog.showAndWait();
        return dialog.getResult();
    }

    private int getMatrizDimension(ArrayList<Integer> numbers) {
        var choices = new ArrayList<Integer>();
        numbers.forEach(n -> { choices.add(n); });
        var dialog = new ChoiceDialog<Integer>(2, choices);
        dialog.setTitle("Dimensão da Matriz");
        dialog.setHeaderText("Escolha a dimensão da matriz de dithering");
        dialog.showAndWait();
        return dialog.getResult();
    }

    private void setDisabledTabs(boolean bolean) {
        tab0.setDisable(bolean);
        tab1.setDisable(bolean);
        tab2.setDisable(bolean);
        tab3.setDisable(bolean);
    }
}

class PButton extends Button {
    MethodType methodType;

    public PButton(String title, MethodType methodType) {
        super(title);
        this.methodType = methodType;
    }
}

enum MethodType {
    limiarSimples, moduladoAleatorio, periodicoDisperso, periodicoAglomerado, aperiodicoDisperso,
    erosao, dilatacao, abertura, fechamento, bordaInterna, bordaExterna, smoothing, gradiente,
    crescimentoRegiao;

    public String description(String comp) {
        switch (this) {
            case limiarSimples: return "Limiar Simples" + comp;
            case moduladoAleatorio: return "Modula Aleatório" + comp;
            case periodicoDisperso: return "Periodico Disperso" + comp;
            case periodicoAglomerado: return "Perióico Aglomerado" + comp;
            case aperiodicoDisperso: return "Aperiódico Disperso" + comp;
            case erosao: return "Erosão" + comp;
            case dilatacao: return "Dialtação" + comp;
            case abertura: return "Abertura" + comp;
            case fechamento: return "Fechamento" + comp;
            case bordaInterna: return "Borda Interna" + comp;
            case bordaExterna: return "Borda Externa" + comp;
            case gradiente:  return "Gradiente" + comp;
            case smoothing: return "Smoothing" + comp;
            case crescimentoRegiao: return "Crescimento de Região" + comp;
        }
        return null;
    }
}