package ru.geekbrains.level_3.hw_5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;



public class MainClass {
    public static final int CARS_COUNT = 4;
    public static final CountDownLatch START_LATCH = new CountDownLatch(CARS_COUNT+1);
    public static final CountDownLatch FINISH_LATCH = new CountDownLatch(CARS_COUNT+1);
    public static final Semaphore SEMAPHORE = new Semaphore(CARS_COUNT/2,true);
    public static boolean flag=true;

    public static void main(String[] args) {
        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
        Race race = new Race(new Road(60), new Tunnel(), new Road(40));
        Car[] cars = new Car[CARS_COUNT];

        for (int i = 0; i < cars.length; i++) {
            new Thread(new Car(race, 20 + (int) (Math.random() * 10), "Участник № " + (i + 1))).start();
        }


        Thread pushMessages = new Thread(new Runnable() {
            @Override
            public void run() {
                    START_LATCH.countDown();
                    System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
                    FINISH_LATCH.countDown();
                try {
                    FINISH_LATCH.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");

            }
        });
        pushMessages.start();
    }


    private static class Car implements Runnable {
        Object winner = new Object();
        private Race race;
        private int speed;
        private String name;

        public String getName() {
            return name;
        }

        public int getSpeed() {
            return speed;
        }

        public Car(Race race, int speed, String name) {
            this.race = race;
            this.speed = speed;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                System.out.println(this.name + " подъехал к старту");
                START_LATCH.countDown();
                System.out.println(this.name + " готов");
                START_LATCH.await();
                race.getStages().get(0).go(this);
                System.out.println(this.name+" готовится к этапу(ждет): " + race.getStages().get(1).getDescription());
                SEMAPHORE.acquire();
                race.getStages().get(1).go(this);
                SEMAPHORE.release();
                race.getStages().get(2).go(this);
                    synchronized (winner) {
                        if(flag) {
                        System.out.println(this.name + " пришел к финишу первым");
                        flag = false;
                       Thread.sleep(100);
                    }
                }
                FINISH_LATCH.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    abstract static class Stage {
        protected int length;
        protected String description;
        public String getDescription() {
            return description;
        }
        public abstract void go(Car c);
    }

   private static   class Race {
        private ArrayList<Stage> stages;
        public ArrayList<Stage> getStages() { return stages; }
        public Race(Stage... stages) {
            this.stages = new ArrayList<>(Arrays.asList(stages));
        }
    }

    private static class Tunnel extends Stage {
        public Tunnel() {
            this.length = 80;
            this.description = "Тоннель " + length + " метров";
        }
        @Override
        public void go(Car c) {
            try {
                try {
                    System.out.println(c.getName() + " начал этап: " + description);
                    Thread.sleep(length / c.getSpeed() * 1000);
                    System.out.println(c.getName() + " закончил этап: " + description);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class  Road extends Stage {
        public Road(int length) {
            this.length = length;
            this.description = "Дорога " + length + " метров";
        }
        @Override
        public void go(Car c) {
            try {
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
                System.out.println(c.getName() + " закончил этап: " + description);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}


