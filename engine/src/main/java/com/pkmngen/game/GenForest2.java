package com.pkmngen.game;

import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class GenForest2 extends Action {
   public Action.Layer layer = Action.Layer.map_120;
   ArrayList<Vector2> freePositions;
   HashMap<Vector2, Tile> tilesToAdd;
   ArrayList<Action> doActions;
   Vector2 topLeft;
   Vector2 bottomRight;
   Random rand = new Random();
   public static HashMap<String, String> mates = new HashMap<>();
   public static HashMap<String, String> mates2 = new HashMap<>();

   public static void FlipFormat(String[][] format) {
      int i = 0;

      for (String[] sub : format) {
         int j = 0;

         for (String tileName : sub) {
            if (i >= 5 - j) {
               break;
            }

            String temp = format[i][j];
            format[i][j] = format[5 - j][5 - i];
            format[5 - j][5 - i] = temp;
            j++;
         }

         i++;
      }
   }

   public static ArrayList<Tile> GenTiles(String[][] format, int offSetX, int offSetY, Random rand) {
      return GenTiles(format, offSetX, offSetY, rand, false);
   }

   public static ArrayList<Tile> GenTiles(String[][] format, int offSetX, int offSetY, Random rand, boolean color) {
      ArrayList<Tile> tiles = new ArrayList<>();
      String[] emptyOrSolid = new String[]{"solid", ""};
      int i = 5;

      for (String[] sub : format) {
         int j = 0;

         for (String tileName : sub) {
            if (tileName == "either") {
               int randomNum = rand.nextInt(emptyOrSolid.length);
               tileName = emptyOrSolid[randomNum];
            }

            if (tileName != "") {
               tiles.add(new Tile(tileName, new Vector2(0 + j * 16 + offSetX, 0 + i * 16 + offSetY), color));
            }

            j++;
         }

         i--;
      }

      return tiles;
   }

   public static ArrayList<Tile> getTileSquare(MazeNode node, Vector2 startLoc, Random rand) {
      return getTileSquare(node, startLoc, rand, false);
   }

   public static ArrayList<Tile> getTileSquare(MazeNode node, Vector2 startLoc, Random rand, boolean color) {
      int offSetx = node.size * node.x + (int)startLoc.x;
      int offSetY = node.size * node.y + (int)startLoc.y;
      ArrayList<ArrayList<Tile>> squares = new ArrayList<>();
      new ArrayList();
      String[][] format = new String[0][];
      String[] ledge = new String[]{"ledge_grass_down", "ledge_grass_down", "ledge_grass_down", "ledge_grass_down", "ledge_grass_down", "ledge_grass_down"};
      ledge[rand.nextInt(ledge.length)] = "ledge_grass_ramp";
      if (Arrays.equals(node.isOpen, new boolean[]{false, false})) {
         format = new String[][]{
            {"solid", "solid", "", "", "either", "either"},
            {"solid", "solid", "solid", "", "", "either"},
            {"solid", "solid", "solid", "solid", "", ""},
            {"solid", "solid", "solid", "solid", "solid", ""},
            {"solid", "solid", "solid", "solid", "solid", "solid"},
            {"solid", "solid", "solid", "solid", "solid", "solid"}
         };
         ArrayList<Tile> square = GenTiles(format, offSetx, offSetY, rand, color);
         squares.add(square);
         format = new String[][]{
            {"solid", "solid", "", "", "solid", "solid"},
            {"solid", "solid", "", "", "solid", "solid"},
            {"solid", "solid", "either", "", "", ""},
            {"solid", "solid", "either", "either", "", ""},
            {"solid", "solid", "solid", "solid", "solid", "solid"},
            {"solid", "solid", "solid", "solid", "solid", "solid"}
         };
         square = GenTiles(format, offSetx, offSetY, rand, color);
         squares.add(square);
      } else if (node.leftOpen ^ node.downOpen) {
         format = new String[][]{
            {"either", "either", "", "", "either", "either"},
            {"", "", "", "", "", ""},
            {"", "", "", "", "", ""},
            {"", "either", "", "either", "", ""},
            {"solid", "solid", "solid", "solid", "solid", "solid"},
            {"solid", "solid", "solid", "solid", "solid", "solid"}
         };
         if (node.downOpen) {
            FlipFormat(format);
            if (rand.nextInt(3) == 1) {
               int randRowIndex = rand.nextInt(format.length - 2) + 1;
               String[] randRow = format[randRowIndex];
               boolean onGround = false;
               ArrayList<Integer> ledgesGoHere = new ArrayList<>();

               for (int i = 0; i < randRow.length; i++) {
                  if (randRow[i] != "solid") {
                     onGround = true;
                     ledgesGoHere.add(i);
                  } else if (onGround) {
                     break;
                  }
               }

               int randIndexForRamp = ledgesGoHere.get(rand.nextInt(ledgesGoHere.size()));

               for (Integer index : ledgesGoHere) {
                  randRow[index] = "ledge_grass_down";
                  if (index == randIndexForRamp) {
                     randRow[index] = "ledge_grass_ramp";
                     format[randRowIndex - 1][index] = "";
                     format[randRowIndex + 1][index] = "";
                  }
               }
            }
         }

         ArrayList<Tile> var18 = GenTiles(format, offSetx, offSetY, rand, color);
         squares.add(var18);
         format = new String[][]{
            {"", "", "", "", "solid", "solid"},
            {"", "", "", "solid", "solid", "solid"},
            {"", "", "", "", "", ""},
            {"", "", "", "", "", ""},
            {"solid", "solid", "solid", "solid", "solid", "solid"},
            {"solid", "solid", "solid", "solid", "solid", "solid"}
         };
         if (node.downOpen) {
            FlipFormat(format);
            if (rand.nextInt(3) == 1) {
               String[] randRow = format[rand.nextInt(format.length - 2) + 1];
               boolean onGround = false;
               ArrayList<Integer> ledgesGoHere = new ArrayList<>();

               for (int i = 0; i < randRow.length; i++) {
                  if (randRow[i] != "solid") {
                     onGround = true;
                     ledgesGoHere.add(i);
                  } else if (onGround) {
                     break;
                  }
               }

               int randIndexForRamp = ledgesGoHere.get(rand.nextInt(ledgesGoHere.size()));

               for (Integer index : ledgesGoHere) {
                  randRow[index] = "ledge_grass_down";
                  if (index == randIndexForRamp) {
                     randRow[index] = "ledge_grass_ramp";
                  }
               }
            }
         }

         var18 = GenTiles(format, offSetx, offSetY, rand, color);
         squares.add(var18);
      } else {
         format = new String[][]{
            {"", "", "", "", "solid", "solid"},
            {"", "", "", "either", "solid", "solid"},
            {"", "", "", "", "either", ""},
            {"", "either", "", "", "", ""},
            {"solid", "solid", "either", "", "", ""},
            {"solid", "solid", "", "", "", ""}
         };
         ArrayList<Tile> var20 = GenTiles(format, offSetx, offSetY, rand, color);
         squares.add(var20);
         format = new String[][]{
            {"", "", "", "", "", ""},
            {"", "", "", "", "", ""},
            {"", "", "", "", "", ""},
            {"", "", "", "", "", ""},
            {"solid", "solid", "", "", "", ""},
            {"solid", "solid", "", "", "", ""}
         };
         var20 = GenTiles(format, offSetx, offSetY, rand, color);
         squares.add(var20);
      }

      int randomNum = rand.nextInt(squares.size());
      return squares.get(randomNum);
   }

   public static ArrayList<Tile> getTileSquarePlatform1(MazeNode node, Vector2 startLoc, Random rand) {
      int offSetx = node.size * node.x + (int)startLoc.x;
      int offSetY = node.size * node.y + (int)startLoc.y;
      ArrayList<ArrayList<Tile>> squares = new ArrayList<>();
      new ArrayList();
      String[][] format = new String[0][];
      String[] temp = new String[]{"bush1", "tree_small1"};
      String[] rs = new String[]{temp[rand.nextInt(temp.length)], "ground1", "ground1"};
      if (!node.leftOpen && !node.downOpen) {
         if (node.rampLoc == "down") {
            format = new String[][]{
               {"tree_large1_noSprite", "tree_large1_noSprite", "ledge_grass_left", "", "", "ledge_grass_right"},
               {"tree_large1", "tree_large1_noSprite", "ledge_grass_left", "", "", "ledge_grass_right"},
               {"tree_large1_noSprite", "tree_large1_noSprite", "ledge_grass_left", "ledge_grass_inside_tl", "ledge_grass_ramp", "ledge1_corner_br"},
               {"tree_large1", "tree_large1_noSprite", "ledge1_corner_bl", "ledge1_corner_br", "", ""},
               {"tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite"},
               {"tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite"}
            };
            ArrayList<Tile> var20 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var20);
         } else {
            format = new String[][]{
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "ledge2_corner_tl", "ledge_grass_safari_up", "ledge_grass_safari_up"},
               {"tree_large1", "tree_large1_noSprite", "", "ledge_grass_left", "", ""},
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "ledge1_corner_bl", "ledge_grass_ramp", "ledge_grass_inside_tr"},
               {"tree_large1", "tree_large1_noSprite", "", "", "", "ledge1_corner_bl"},
               {"tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite"},
               {"tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite"}
            };
            ArrayList<Tile> var21 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var21);
         }
      } else if (node.leftOpen && !node.downOpen) {
         if (node.rampLoc == "down") {
            format = new String[][]{
               {
                     rs[rand.nextInt(rs.length)],
                     rs[rand.nextInt(rs.length)],
                     rs[rand.nextInt(rs.length)],
                     rs[rand.nextInt(rs.length)],
                     rs[rand.nextInt(rs.length)],
                     rs[rand.nextInt(rs.length)]
               },
               {"ledge_grass_left", "", "", "", "", "ledge_grass_right"},
               {"ledge1_corner_bl", "ledge_grass_down", "ledge_grass_down", "ledge_grass_ramp", "ledge_grass_down", "ledge1_corner_br"},
               {"", "", "", "", "", ""},
               {"tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite"},
               {"tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite"}
            };
            ArrayList<Tile> var17 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var17);
         } else if (node.rampLoc == "left") {
            format = new String[][]{
               {"", "", "", "ledge2_corner_tl", "ledge_grass_safari_up", "ledge_grass_safari_up"},
               {"", "", "", "ledge_grass_left", "", ""},
               {"", "", "", "ledge1_corner_bl", "ledge_grass_ramp", "ledge_grass_inside_tr"},
               {"", "", "", "", "", "ledge1_corner_bl"},
               {"tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite"},
               {"tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite"}
            };
            ArrayList<Tile> var18 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var18);
         } else {
            format = new String[][]{
               {"ledge_grass_safari_up", "ledge_grass_safari_up", "ledge_grass_safari_up", "ledge2_corner_tr", "", ""},
               {"", "", "", "ledge_grass_right", "", ""},
               {"", "ledge_grass_inside_tl", "ledge_grass_ramp", "ledge1_corner_br", "", ""},
               {"ledge_grass_down", "ledge1_corner_br", "", "", "", ""},
               {"tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite", "tree_large1_noSprite"},
               {"tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite", "tree_large1", "tree_large1_noSprite"}
            };
            ArrayList<Tile> var19 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var19);
         }
      } else if (!node.leftOpen && node.downOpen) {
         if (node.rampLoc == "down") {
            format = new String[][]{
               {"tree_large1_noSprite", "tree_large1_noSprite", "ledge_grass_left", "", "", "ledge_grass_right"},
               {"tree_large1", "tree_large1_noSprite", "ledge_grass_left", "", "", "ledge_grass_right"},
               {"tree_large1_noSprite", "tree_large1_noSprite", "ledge1_corner_bl", "ledge_grass_ramp", "ledge_grass_down", "ledge1_corner_br"},
               {"tree_large1", "tree_large1_noSprite", "", "", "", ""},
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "", "", ""},
               {"tree_large1", "tree_large1_noSprite", "", "", "", ""}
            };
            ArrayList<Tile> var14 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var14);
         } else if (node.rampLoc == "left") {
            format = new String[][]{
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "ledge2_corner_tl", "ledge_grass_safari_up", "ledge_grass_safari_up"},
               {"tree_large1", "tree_large1_noSprite", "", "ledge_grass_left", "", ""},
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "ledge1_corner_bl", "ledge_grass_ramp", "ledge_grass_inside_tr"},
               {"tree_large1", "tree_large1_noSprite", "", "", "", "ledge1_corner_bl"},
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "", "", ""},
               {"tree_large1", "tree_large1_noSprite", "", "", "", ""}
            };
            ArrayList<Tile> var15 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var15);
         } else {
            format = new String[][]{
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "", "", ""},
               {"tree_large1", "tree_large1_noSprite", "ledge_grass_safari_up", "ledge_grass_safari_up", "ledge_grass_safari_up", "ledge2_corner_tr"},
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "", "", "ledge_grass_right"},
               {"tree_large1", "tree_large1_noSprite", "", "ledge_grass_inside_tl", "ledge_grass_ramp", "ledge1_corner_br"},
               {"tree_large1_noSprite", "tree_large1_noSprite", "", "ledge_grass_right", "", ""},
               {"tree_large1", "tree_large1_noSprite", "", "ledge_grass_safari_up", "ledge_grass_safari_up", "ledge2_corner_tr"}
            };
            ArrayList<Tile> var16 = GenTiles(format, offSetx, offSetY, rand);
            squares.add(var16);
         }
      } else if (node.rampLoc == "down") {
         format = new String[][]{
            {"", "", "ledge_grass_left", "", "", "ledge_grass_right"},
            {"", "", "ledge1_corner_bl", "ledge_grass_ramp", "ledge_grass_down", "ledge1_corner_br"},
            {"", "", "", "", rs[rand.nextInt(rs.length)], ""},
            {"", rs[rand.nextInt(rs.length)], "", "", "", ""},
            {"tree_large1_noSprite", "tree_large1_noSprite", rs[rand.nextInt(rs.length)], "", "", ""},
            {"tree_large1", "tree_large1_noSprite", "", "", "", ""}
         };
         ArrayList<Tile> square = GenTiles(format, offSetx, offSetY, rand);
         squares.add(square);
      } else if (node.rampLoc == "up") {
         format = new String[][]{
            {"", "", "", "", "", ""},
            {"", "ledge2_corner_tl", "ledge_grass_safari_up", "ledge_grass_safari_up", "ledge_grass_safari_up", "ledge2_corner_tr"},
            {"", "ledge_grass_left", "", "", "", "ledge_grass_right"},
            {"", "ledge1_corner_bl", "ledge_grass_ramp", "ledge_grass_inside_tr", "", "ledge_grass_right"},
            {"", "", "", "ledge_grass_left", "", "ledge_grass_right"},
            {"", "", "ledge2_corner_tl", "ledge_grass_safari_up", "", "ledge_grass_right"}
         };
         ArrayList<Tile> var11 = GenTiles(format, offSetx, offSetY, rand);
         squares.add(var11);
      } else if (node.rampLoc == "left") {
         format = new String[][]{
            {"", "", "ledge_grass_left", "", "", ""},
            {"", "", "ledge_grass_left", "", "", ""},
            {"", "", "ledge1_corner_br", "ledge_grass_ramp", "ledge_grass_inside_tr", ""},
            {"", "", "", "", "ledge_grass_left", ""},
            {"tree_large1_noSprite", "tree_large1_noSprite", "", "", "ledge_grass_left", ""},
            {"tree_large1", "tree_large1_noSprite", "", "", "ledge1_corner_br", "ledge_grass_down"}
         };
         if (node.downOpen) {
            FlipFormat(format);
         }

         ArrayList<Tile> var12 = GenTiles(format, offSetx, offSetY, rand);
         squares.add(var12);
      } else {
         format = new String[][]{
            {"", "", "", "ledge_grass_right", "", ""},
            {"", "", "", "ledge_grass_right", "", ""},
            {"", "ledge_grass_inside_tl", "ledge_grass_ramp", "ledge1_corner_br", "", ""},
            {"", "ledge_grass_right", "", "", "", ""},
            {"", "ledge_grass_right", "", "", "tree_large1_noSprite", "tree_large1_noSprite"},
            {"ledge_grass_down", "ledge1_corner_br", "", "", "tree_large1", "tree_large1_noSprite"}
         };
         if (node.downOpen) {
            FlipFormat(format);
         }

         ArrayList<Tile> var13 = GenTiles(format, offSetx, offSetY, rand);
         squares.add(var13);
      }

      int randomNum = rand.nextInt(squares.size());
      return squares.get(randomNum);
   }

   public static boolean[][] Maze_Algo2(int width, int height, float complexity, float density, Random rand) {
      width = width / 2 * 2 + 2;
      height = height / 2 * 2 + 2;
      complexity = (int)(complexity * (5 * (height + width)));
      density = (int)(density * (height / 2 * (width / 2)));
      boolean[][] Z = new boolean[width][height];

      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            if (i == 0 || j == 0 || i == width - 2 || j == height - 2) {
               Z[i][j] = Boolean.TRUE;
            }
         }
      }

      for (int i = 0; i < density; i++) {
         int x = rand.nextInt(width / 2) * 2;
         int y = rand.nextInt(height / 2) * 2;
         Z[x][y] = Boolean.TRUE;

         for (int j = 0; j < complexity; j++) {
            ArrayList<int[]> neighbours = new ArrayList<>();
            if (x > 1) {
               neighbours.add(new int[]{x - 2, y});
            }

            if (x < width - 2) {
               neighbours.add(new int[]{x + 2, y});
            }

            if (y > 1) {
               neighbours.add(new int[]{x, y - 2});
            }

            if (y < height - 2) {
               neighbours.add(new int[]{x, y + 2});
            }

            if (!neighbours.isEmpty()) {
               int randomNum = rand.nextInt(neighbours.size());
               int x_ = neighbours.get(randomNum)[0];
               int y_ = neighbours.get(randomNum)[1];
               if (Z[x_][y_] == Boolean.FALSE) {
                  Z[x_][y_] = Boolean.TRUE;
                  Z[x_ + (x - x_) / 2][y_ + (y - y_) / 2] = Boolean.TRUE;
                  x = x_;
                  y = y_;
               }
            }
         }
      }

      for (int i = height - 1; i >= 0; i--) {
         for (int j = 0; j < width; j++) {
            System.out.print((Z[j][i] ? 1 : 0) + " ");
         }

         System.out.print("\n");
      }

      return Z;
   }

   public static boolean[][] Maze_Algo3(int width, int height, float complexity, float density, Random rand, ArrayList<Vector2> startLocs) {
      width = width / 2 * 2 + 2;
      height = height / 2 * 2 + 2;
      ArrayList<Vector2> endpoints = new ArrayList<>();
      boolean[][] Z = new boolean[width][height];
      boolean placedOneWall = false;

      for (int i = 0; i < density; i++) {
         int x;
         int y;
         if (!startLocs.isEmpty()) {
            Vector2 newLoc = startLocs.remove(0);
            x = (int)newLoc.x;
            y = (int)newLoc.y;
         } else {
            x = rand.nextInt(width / 2) * 2;
            y = rand.nextInt(height / 2) * 2;
         }

         Z[x][y] = Boolean.TRUE;
         placedOneWall = false;

         for (int j = 0; j < complexity; j++) {
            ArrayList<int[]> neighbours = new ArrayList<>();
            if (x > 1) {
               neighbours.add(new int[]{x - 2, y});
            }

            if (x < width - 2) {
               neighbours.add(new int[]{x + 2, y});
            }

            if (y > 1) {
               neighbours.add(new int[]{x, y - 2});
            }

            if (y < height - 2) {
               neighbours.add(new int[]{x, y + 2});
            }

            if (!neighbours.isEmpty()) {
               int randomNum = rand.nextInt(neighbours.size());
               int x_ = neighbours.get(randomNum)[0];
               int y_ = neighbours.get(randomNum)[1];
               if (Z[x_][y_] == Boolean.FALSE) {
                  Z[x_][y_] = Boolean.TRUE;
                  Z[x_ + (x - x_) / 2][y_ + (y - y_) / 2] = Boolean.TRUE;
                  x = x_;
                  y = y_;
                  placedOneWall = true;
               }
            }
         }

         if (placedOneWall) {
            endpoints.add(new Vector2(x, y));
         }
      }

      for (int i = height - 1; i >= 0; i--) {
         for (int j = 0; j < width; j++) {
            System.out.print((Z[j][i] ? 1 : 0) + " ");
         }

         System.out.print("\n");
      }

      startLocs.clear();

      for (Vector2 point : endpoints) {
         startLocs.add(point);
      }

      return Z;
   }

   public static HashMap<Vector2, MazeNode> Maze_Algo1(int width, int height, float complexity, float density, int squareSize, Random rand) {
      width = width / 2 * 2 + 2;
      height = height / 2 * 2 + 2;
      complexity = (int)(complexity * (5 * (height + width)));
      density = (int)(density * (height / 2 * (width / 2)));
      boolean[][] Z = new boolean[width][height];

      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            if (i == 0 || i == width - 2 || j == 0 || j == height - 2) {
               Z[i][j] = Boolean.TRUE;
            }
         }
      }

      for (int i = 0; i < density; i++) {
         int x = rand.nextInt(width / 2) * 2;
         int y = rand.nextInt(height / 2) * 2;
         Z[x][y] = Boolean.TRUE;

         for (int j = 0; j < complexity; j++) {
            ArrayList<int[]> neighbours = new ArrayList<>();
            if (x > 1) {
               neighbours.add(new int[]{x - 2, y});
            }

            if (x < width - 2) {
               neighbours.add(new int[]{x + 2, y});
            }

            if (y > 1) {
               neighbours.add(new int[]{x, y - 2});
            }

            if (y < height - 2) {
               neighbours.add(new int[]{x, y + 2});
            }

            if (!neighbours.isEmpty()) {
               int randomNum = rand.nextInt(neighbours.size());
               int x_ = neighbours.get(randomNum)[0];
               int y_ = neighbours.get(randomNum)[1];
               if (Z[x_][y_] == Boolean.FALSE) {
                  Z[x_][y_] = Boolean.TRUE;
                  Z[x_ + (x - x_) / 2][y_ + (y - y_) / 2] = Boolean.TRUE;
                  x = x_;
                  y = y_;
               }
            }
         }
      }

      for (int i = height - 1; i >= 0; i--) {
         for (int j = 0; j < width; j++) {
            System.out.print((Z[j][i] ? 1 : 0) + " ");
         }

         System.out.print("\n");
      }

      HashMap<Vector2, MazeNode> nodes = new HashMap<>();

      for (int i = 0; i < width / 2; i++) {
         for (int j = 0; j < height / 2; j++) {
            boolean leftOpen = !Z[i * 2][j * 2 + 1];
            boolean downOpen = !Z[i * 2 + 1][j * 2];
            nodes.put(new Vector2(i, j), new MazeNode(i, j, new boolean[]{leftOpen, downOpen}, squareSize));
         }
      }

      return nodes;
   }

   public static HashMap<Vector2, MazeNode> Maze_Algo4(int width, int height, float complexity, float density, int squareSize, Random rand) {
      width = width / 2 * 2 + 2;
      height = height / 2 * 2 + 2;
      boolean[][] Z = new boolean[width][height];

      for (int i = 0; i < width; i++) {
         for (int j = 0; j < height; j++) {
            if (i == 0 || i == width - 2 || j == 0 || j == height - 2) {
               Z[i][j] = Boolean.TRUE;
            }
         }
      }

      for (int i = 0; i < density; i++) {
         int x = rand.nextInt(width / 2) * 2;
         int y = rand.nextInt(height / 2) * 2;
         Z[x][y] = Boolean.TRUE;

         for (int j = 0; j < complexity; j++) {
            ArrayList<int[]> neighbours = new ArrayList<>();
            if (x > 1) {
               neighbours.add(new int[]{x - 2, y});
            }

            if (x < width - 2) {
               neighbours.add(new int[]{x + 2, y});
            }

            if (y > 1) {
               neighbours.add(new int[]{x, y - 2});
            }

            if (y < height - 2) {
               neighbours.add(new int[]{x, y + 2});
            }

            if (!neighbours.isEmpty()) {
               int randomNum = rand.nextInt(neighbours.size());
               int x_ = neighbours.get(randomNum)[0];
               int y_ = neighbours.get(randomNum)[1];
               if (Z[x_][y_] == Boolean.FALSE) {
                  Z[x_][y_] = Boolean.TRUE;
                  Z[x_ + (x - x_) / 2][y_ + (y - y_) / 2] = Boolean.TRUE;
                  x = x_;
                  y = y_;
               }
            }
         }
      }

      for (int i = height - 1; i >= 0; i--) {
         for (int j = 0; j < width; j++) {
            System.out.print((Z[j][i] ? 1 : 0) + " ");
         }

         System.out.print("\n");
      }

      HashMap<Vector2, MazeNode> nodes = new HashMap<>();

      for (int i = 0; i < width / 2; i++) {
         for (int j = 0; j < height / 2; j++) {
            boolean leftOpen = !Z[i * 2][j * 2 + 1];
            boolean downOpen = !Z[i * 2 + 1][j * 2];
            nodes.put(new Vector2(i, j), new MazeNode(i, j, new boolean[]{leftOpen, downOpen}, squareSize));
         }
      }

      return nodes;
   }

   public GenForest2(Game game, Vector2 startLoc, Vector2 endLoc) {
      super();
      this.tilesToAdd = new HashMap<>();
      this.freePositions = new ArrayList<>();
      this.doActions = new ArrayList<>();
      int squareSize = 96;
      int width = (int)((endLoc.x - startLoc.x) / squareSize) * 2;
      int height = (int)((endLoc.y - startLoc.y) / squareSize) * 2;
      float density = 0.1F;
      float complexity = 0.9F;
      HashMap<Vector2, MazeNode> nodes = Maze_Algo1(width, height, density, complexity, squareSize, this.rand);

      for (MazeNode node : nodes.values()) {
         ArrayList<Tile> tileSquare;
         if (node.type == "platform1") {
            tileSquare = getTileSquarePlatform1(node, startLoc, this.rand);
         } else {
            tileSquare = getTileSquare(node, startLoc, this.rand);
         }

         for (Tile tile : tileSquare) {
            this.tilesToAdd.put(tile.position.cpy(), tile);
         }
      }

      Action temp = new GenForest2.ApplyForestBiome(this.tilesToAdd, startLoc, endLoc.cpy(), this);
      temp.step(game);
   }

   @Override
   public Action.Layer getLayer() {
      return this.layer;
   }

   @Override
   public void step(Game game) {
      if (this.tilesToAdd.isEmpty()) {
         if (this.doActions.isEmpty()) {
            game.actionStack.remove(this);
            game.map.tiles.put(new Vector2(0.0F, 64.0F), new MegaGengarTile(new Vector2(0.0F, 64.0F)));
         } else {
            Action currAction = this.doActions.get(0);
            this.doActions.remove(0);
            game.insertAction(currAction);
            game.actionStack.remove(this);
         }
      } else {
         Tile currTile = this.tilesToAdd.values().iterator().next();
         game.map.tiles.put(currTile.position.cpy(), currTile);
         this.tilesToAdd.remove(currTile.position.cpy());

         for (int i = 0; i < 10 && !this.tilesToAdd.isEmpty(); i++) {
            currTile = this.tilesToAdd.values().iterator().next();
            game.map.tiles.put(currTile.position.cpy(), currTile);
            this.tilesToAdd.remove(currTile.position.cpy());
         }
      }
   }

   static {
      mates.put("nidoqueen", "nidoking");
      mates.put("nidoking", "nidoqueen");
      mates.put("charizard", "charizard");
      mates.put("venusaur", "venusaur");
      mates.put("meganium", "meganium");
      mates.put("nidorina", "nidorino");
      mates.put("nidorino", "nidorina");
      mates2.put("tauros", "miltank");
      mates2.put("miltank", "tauros");
   }

   public class AddPlatform extends Action {
      public Action.Layer layer = Action.Layer.map_120;
      Random rand;
      ArrayList<Tile> tilesToAdd;
      ArrayList<Vector2> freePositions;
      Vector2 startPosition;
      Action nextAction;
      int numLevels;
      Vector2 bottomLeft;
      Vector2 topRight;

      public AddPlatform(Game game, ArrayList<Tile> tilesToAdd, ArrayList<Vector2> freePositions, Vector2 startLoc, Vector2 endLoc, Action nextAction) {
         super();
         this.nextAction = nextAction;
         this.rand = new Random();
         this.freePositions = freePositions;
         this.tilesToAdd = tilesToAdd;
         int x = this.rand.nextInt((int)(endLoc.x - startLoc.x)) + (int)startLoc.x;
         int y = this.rand.nextInt((int)(endLoc.y - startLoc.y)) + (int)startLoc.y;
         int width = (int)(
            0.5F * (endLoc.x - startLoc.x)
               + (this.rand.nextInt(10) + 1) / 10.0F * 0.33333334F * (endLoc.x - startLoc.x)
               - 0.16666667F * (endLoc.x - startLoc.x)
         );
         int height = (int)(
            0.5F * (endLoc.y - startLoc.y)
               + (this.rand.nextInt(10) + 1) / 10.0F * 0.33333334F * (endLoc.y - startLoc.y)
               - 0.16666667F * (endLoc.y - startLoc.y)
         );
         this.bottomLeft = new Vector2(x - width / 2, y - height / 2);
         this.topRight = new Vector2(x + width / 2, y + height / 2);
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      @Override
      public void step(Game game) {
         ArrayList<Vector2> platformGoesHere = new ArrayList<>();

         for (Vector2 pos : this.freePositions) {
            if (pos.x >= this.bottomLeft.x && pos.x <= this.topRight.x && pos.y >= this.bottomLeft.y && pos.y <= this.topRight.y) {
               platformGoesHere.add(pos.cpy());
            }
         }

         if (platformGoesHere.isEmpty()) {
            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         } else {
            ArrayList<Vector2> bottomLedges = new ArrayList<>();

            for (Vector2 pos : platformGoesHere) {
               boolean upPlatform = false;
               boolean leftPlatform = false;
               boolean rightPlatform = false;
               boolean downPlatform = false;
               if (platformGoesHere.contains(new Vector2(pos.x + 16.0F, pos.y))) {
                  rightPlatform = true;
               }

               if (platformGoesHere.contains(new Vector2(pos.x - 16.0F, pos.y))) {
                  leftPlatform = true;
               }

               if (platformGoesHere.contains(new Vector2(pos.x, pos.y + 16.0F))) {
                  upPlatform = true;
               }

               if (platformGoesHere.contains(new Vector2(pos.x, pos.y - 16.0F))) {
                  downPlatform = true;
               }

               if (rightPlatform && !leftPlatform && upPlatform && downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge_grass_left", new Vector2(pos.x, pos.y)));
               } else if (!rightPlatform && leftPlatform && upPlatform && downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge_grass_right", new Vector2(pos.x, pos.y)));
               } else if (rightPlatform && leftPlatform && !upPlatform && downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge_grass_safari_up", new Vector2(pos.x, pos.y)));
               } else if (rightPlatform && leftPlatform && upPlatform && !downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge_grass_down", new Vector2(pos.x, pos.y)));
                  bottomLedges.add(pos);
               } else if (rightPlatform && !leftPlatform && upPlatform && !downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge1_corner_bl", new Vector2(pos.x, pos.y)));
               } else if (!rightPlatform && leftPlatform && upPlatform && !downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge1_corner_br", new Vector2(pos.x, pos.y)));
               } else if (rightPlatform && !leftPlatform && !upPlatform && downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge2_corner_tl", new Vector2(pos.x, pos.y)));
               } else if (!rightPlatform && leftPlatform && !upPlatform && downPlatform) {
                  this.tilesToAdd.add(new Tile("ledge2_corner_tr", new Vector2(pos.x, pos.y)));
               } else if (!rightPlatform && !leftPlatform && !upPlatform && !downPlatform) {
                  this.tilesToAdd.add(new Tile("rock1", new Vector2(pos.x, pos.y)));
               } else {
                  this.tilesToAdd.add(new Tile("ground1", new Vector2(pos.x, pos.y)));
               }

               this.freePositions.remove(pos);
            }

            if (!bottomLedges.isEmpty()) {
               this.tilesToAdd.add(new Tile("ledge_grass_ramp", bottomLedges.get(this.rand.nextInt(bottomLedges.size())).cpy()));
               if (this.rand.nextInt(2) == 1) {
                  this.tilesToAdd.add(new Tile("ledge_grass_ramp", bottomLedges.get(this.rand.nextInt(bottomLedges.size())).cpy()));
               }
            }

            game.insertAction(this.nextAction);
            game.actionStack.remove(this);
         }
      }
   }

   public static class ApplyForestBiome extends Action {
      public Action.Layer layer = Action.Layer.map_120;
      Random rand;
      HashMap<Vector2, Tile> tilesToAdd;
      ArrayList<Vector2> freePositions;
      Action nextAction;
      Vector2 bottomLeft;
      Vector2 topRight;
      boolean color;
      Route currRoute;

      public ApplyForestBiome(HashMap<Vector2, Tile> tilesToAdd, Vector2 bottomLeft, Vector2 topRight, Action nextAction) {
         this(tilesToAdd, bottomLeft, topRight, false, nextAction);
      }

      public ApplyForestBiome(HashMap<Vector2, Tile> tilesToAdd, Vector2 bottomLeft, Vector2 topRight, boolean color, Action nextAction) {
         super();
         this.color = color;
         this.nextAction = nextAction;
         this.rand = new Random();
         this.tilesToAdd = tilesToAdd;
         this.bottomLeft = bottomLeft;
         this.topRight = topRight;
         this.currRoute = new Route("forest1", 22);
      }

      public void addPond() {
         int maxSize = 12;
         ArrayList<Vector2> keySet = new ArrayList<>(this.tilesToAdd.keySet());
         Vector2 randBLCorner = keySet.get(this.rand.nextInt(keySet.size())).cpy();
         Vector2 randTRCorner = randBLCorner.cpy().add(this.rand.nextInt(maxSize) * 16 + 32, this.rand.nextInt(maxSize) * 16 + 32);
         ArrayList<Vector2> pondGoesHere = new ArrayList<>();

         for (float i = randBLCorner.x; i < randTRCorner.x; i += 16.0F) {
            for (float j = randBLCorner.y; j < randTRCorner.y; j += 16.0F) {
               int surroundedBy = 0;
               Tile temp2 = this.tilesToAdd.get(new Vector2(i + 16.0F, j));
               if (temp2 != null && temp2.name == "solid") {
                  surroundedBy++;
               }

               temp2 = this.tilesToAdd.get(new Vector2(i - 16.0F, j));
               if (temp2 != null && temp2.name == "solid") {
                  surroundedBy++;
               }

               temp2 = this.tilesToAdd.get(new Vector2(i, j + 16.0F));
               if (temp2 != null && temp2.name == "solid") {
                  surroundedBy++;
               }

               temp2 = this.tilesToAdd.get(new Vector2(i, j - 16.0F));
               if (temp2 != null && temp2.name == "solid") {
                  surroundedBy++;
               }

               Tile temp = this.tilesToAdd.get(new Vector2(i, j));
               if (temp != null && surroundedBy > 1 && temp.name == "solid") {
                  pondGoesHere.add(temp.position.cpy());
               }
            }
         }

         for (Vector2 pos : pondGoesHere) {
            boolean upPond = false;
            boolean leftPond = false;
            boolean rightPond = false;
            boolean downPond = false;
            if (pondGoesHere.contains(new Vector2(pos.x + 16.0F, pos.y))) {
               rightPond = true;
            }

            if (pondGoesHere.contains(new Vector2(pos.x - 16.0F, pos.y))) {
               leftPond = true;
            }

            if (pondGoesHere.contains(new Vector2(pos.x, pos.y + 16.0F))) {
               upPond = true;
            }

            if (pondGoesHere.contains(new Vector2(pos.x, pos.y - 16.0F))) {
               downPond = true;
            }

            if (rightPond && !leftPond && upPond && downPond) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_left", new Vector2(pos.x, pos.y)));
            } else if (!rightPond && leftPond && upPond && downPond) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_right", new Vector2(pos.x, pos.y)));
            } else if (rightPond && leftPond && !upPond && downPond) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_top", new Vector2(pos.x, pos.y)));
            } else if (rightPond && !leftPond && !upPond && downPond) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_tl", new Vector2(pos.x, pos.y)));
            } else if (!rightPond && leftPond && !upPond && downPond) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_tr", new Vector2(pos.x, pos.y)));
            } else if (rightPond && !leftPond && upPond && !downPond) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_left", new Vector2(pos.x, pos.y)));
            } else if (!rightPond && leftPond && upPond && !downPond) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1_ledge1_right", new Vector2(pos.x, pos.y)));
            } else if ((rightPond ? 1 : 0) + (leftPond ? 1 : 0) + (upPond ? 1 : 0) + (downPond ? 1 : 0) > 1) {
               this.tilesToAdd.put(pos.cpy(), new Tile("water1", new Vector2(pos.x, pos.y)));
            }
         }
      }

      public void fillAllEmptyTiles() {
         String[] randomTile = new String[]{"ground3", "ground1", "ground3", "ground1", "ground3", "ground1", "ground3", "ground1", "flower1"};

         for (float i = this.bottomLeft.x; i < this.topRight.x; i += 16.0F) {
            for (float j = this.bottomLeft.y; j < this.topRight.y; j += 16.0F) {
               Tile temp = this.tilesToAdd.get(new Vector2(i, j));
               String tileName = randomTile[this.rand.nextInt(randomTile.length)];
               if (temp == null && tileName != "") {
                  this.tilesToAdd.put(new Vector2(i, j), new Tile(tileName, new Vector2(i, j)));
               }
            }
         }
      }

      @Override
      public Action.Layer getLayer() {
         return this.layer;
      }

      public void plantGrass() {
         int minNumPatches = 2;
         int maxNumPatches = 12;
         Vector2 startPosition = null;
         int iter = 0;

         while (startPosition == null && iter < 20 && (int)this.topRight.x - (int)this.bottomLeft.x - 64 != 0) {
            iter++;
            int randX = Math.abs((int)this.topRight.x - (int)this.bottomLeft.x - 64) + (int)this.bottomLeft.x;
            randX = this.rand.nextInt(randX <= 0 ? 1 : randX) / 16 * 16;
            int randY = Math.abs((int)this.topRight.y - (int)this.bottomLeft.y - 64) + (int)this.bottomLeft.y;
            randY = this.rand.nextInt(randY <= 0 ? 1 : randY) / 16 * 16;
            Vector2 randomPos = new Vector2(randX, randY);
            if (this.tilesToAdd.get(randomPos.cpy().add(0.0F, 0.0F)) == null
               && this.tilesToAdd.get(randomPos.cpy().add(16.0F, 0.0F)) == null
               && this.tilesToAdd.get(randomPos.cpy().add(0.0F, 16.0F)) == null
               && this.tilesToAdd.get(randomPos.cpy().add(16.0F, 16.0F)) == null) {
               startPosition = randomPos;
            }
         }

         if (startPosition != null) {
            String name = "grass1";
            if (this.color) {
               name = "grass2";
            }

            this.tilesToAdd.put(startPosition.cpy(), new Tile(name, startPosition.cpy(), this.color, this.currRoute));
            this.tilesToAdd.put(startPosition.cpy().cpy().add(16.0F, 0.0F), new Tile(name, startPosition.cpy().add(16.0F, 0.0F), this.color, this.currRoute));
            this.tilesToAdd.put(startPosition.cpy().cpy().add(0.0F, 16.0F), new Tile(name, startPosition.cpy().add(0.0F, 16.0F), this.color, this.currRoute));
            this.tilesToAdd.put(startPosition.cpy().cpy().add(16.0F, 16.0F), new Tile(name, startPosition.cpy().add(16.0F, 16.0F), this.color, this.currRoute));
            Vector2 currPos = startPosition.cpy();
            int randomNum = this.rand.nextInt(maxNumPatches - minNumPatches) + minNumPatches;

            for (int i = 0; i < randomNum; i++) {
               ArrayList<Vector2> nextPositions = new ArrayList<>();
               if (this.tilesToAdd.get(currPos.cpy().add(-16.0F, 0.0F)) == null && this.tilesToAdd.get(currPos.cpy().add(-16.0F, 16.0F)) == null) {
                  nextPositions.add(currPos.cpy().add(-16.0F, 0.0F));
               }

               if (this.tilesToAdd.get(currPos.cpy().add(32.0F, 0.0F)) == null && this.tilesToAdd.get(currPos.cpy().add(32.0F, 16.0F)) == null) {
                  nextPositions.add(currPos.cpy().add(16.0F, 0.0F));
               }

               if (this.tilesToAdd.get(currPos.cpy().add(0.0F, 32.0F)) == null && this.tilesToAdd.get(currPos.cpy().add(16.0F, 32.0F)) == null) {
                  nextPositions.add(currPos.cpy().add(0.0F, 16.0F));
               }

               if (this.tilesToAdd.get(currPos.cpy().add(0.0F, -16.0F)) == null && this.tilesToAdd.get(currPos.cpy().add(16.0F, -16.0F)) == null) {
                  nextPositions.add(currPos.cpy().add(0.0F, -16.0F));
               }

               if (nextPositions.isEmpty()) {
                  break;
               }

               int randomIndex = this.rand.nextInt(nextPositions.size());
               Vector2 newPos = nextPositions.get(randomIndex);
               this.tilesToAdd.put(newPos.cpy(), new Tile(name, newPos.cpy()));
               this.tilesToAdd.put(newPos.cpy().add(16.0F, 0.0F), new Tile(name, newPos.cpy().add(16.0F, 0.0F), this.color, this.currRoute));
               this.tilesToAdd.put(newPos.cpy().add(0.0F, 16.0F), new Tile(name, newPos.cpy().add(0.0F, 16.0F), this.color, this.currRoute));
               this.tilesToAdd.put(newPos.cpy().add(16.0F, 16.0F), new Tile(name, newPos.cpy().add(16.0F, 16.0F), this.color, this.currRoute));
               currPos = newPos;
            }
         }
      }

      @Override
      public void step(Game game) {
         for (float i = this.bottomLeft.x; i < this.topRight.x + 64.0F + 64.0F; i += 16.0F) {
            for (float j = this.bottomLeft.y; j < this.topRight.y + 64.0F + 64.0F + 64.0F; j += 16.0F) {
               Tile temp = this.tilesToAdd.get(new Vector2(i, j));
               if (temp != null && temp.name == "solid") {
                  boolean noTileYet = true;
                  Tile temp2 = this.tilesToAdd.get(new Vector2(i + 16.0F, j));
                  Tile temp3 = this.tilesToAdd.get(new Vector2(i, j + 16.0F));
                  Tile temp4 = this.tilesToAdd.get(new Vector2(i + 16.0F, j + 16.0F));
                  if (Math.abs(temp.position.x - this.bottomLeft.x) % 32.0F == 0.0F
                     && Math.abs(temp.position.y - this.bottomLeft.y) % 32.0F == 0.0F
                     && temp2 != null
                     && temp3 != null
                     && temp4 != null
                     && temp2.name == "solid"
                     && temp3.name == "solid"
                     && temp4.name == "solid") {
                     this.tilesToAdd.put(temp.position.cpy().add(0.0F, 0.0F), new Tile("tree_large1", temp.position.cpy().add(0.0F, 0.0F), this.color));
                     this.tilesToAdd.put(temp.position.cpy().add(16.0F, 0.0F), new Tile("tree_large1_noSprite", temp.position.cpy().add(16.0F, 0.0F)));
                     this.tilesToAdd.put(temp.position.cpy().add(0.0F, 16.0F), new Tile("tree_large1_noSprite", temp.position.cpy().add(0.0F, 16.0F)));
                     this.tilesToAdd.put(temp.position.cpy().add(16.0F, 16.0F), new Tile("tree_large1_noSprite", temp.position.cpy().add(16.0F, 16.0F)));
                     noTileYet = false;
                  }

                  if (noTileYet) {
                     Route tempRoute = new Route("", 11);
                     String[] pokemon = new String[]{
                        "pineco",
                        "aipom",
                        "kakuna",
                        "metapod",
                        "spinarak",
                        "heracross",
                        "ledyba",
                        "hoothoot",
                        "zubat",
                        "pidgey",
                        "spearow",
                        "forretress",
                        "applin"
                     };
                     int randInt = this.rand.nextInt(3);
                     if (randInt == 2) {
                        randInt = this.rand.nextInt(pokemon.length);
                        tempRoute.storedPokemon.add(new Pokemon(pokemon[randInt], 10 + this.rand.nextInt(4)));
                     }

                     this.tilesToAdd.put(temp.position.cpy().add(0.0F, 0.0F), new Tile("bush1", temp.position.cpy().add(0.0F, 0.0F), this.color, tempRoute));
                  }
               }
            }
         }
      }
   }
}
