package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class GtfsProcessExample {

    private final static Logger log = LoggerFactory.getLogger(GtfsProcessExample.class);

    public static void main(String[] args) throws IOException {
        File file = new File("gtfs/libs/gtfs.zip");
        long startTimer = System.currentTimeMillis();
        System.out.println(file.getAbsolutePath());
        GtfsReader r = new GtfsReader();
        r.setInputLocation(file);
        List<Class<?>> entityClasses = new ArrayList<>();
        entityClasses.add(Agency.class);
        entityClasses.add(Stop.class);
        entityClasses.add(Route.class);
        entityClasses.add(Trip.class);
        entityClasses.add(StopTime.class);
        r.setEntityClasses(entityClasses);
        GtfsDaoImpl stuff = new GtfsDaoImpl();
        r.setEntityStore(stuff);
        r.run();
        Map<String, StopRouteInfo> stopRouteInfo = new HashMap<>();

        for (StopTime time : stuff.getAllStopTimes()) {
            Stop stop = time.getStop();
            StopRouteInfo info = stopRouteInfo.get(stop.getCode());
            if (info == null) {
                info = new StopRouteInfo(stop);
                stopRouteInfo.put(info.getStop().getCode(), info);
            }
            info.addRouteToStop(time.getTrip().getRoute().getShortName());
        }

        System.out.println("This took " + (System.currentTimeMillis() - startTimer) + " ms");
        Gson gson = new GsonBuilder().create();
        Type t = new TypeToken<Map<String, StopRouteInfo>>() {}.getType();
        JsonWriter w = new JsonWriter(new FileWriter("data.json"));
        gson.toJson(stopRouteInfo, t, w);
        w.flush();
        w.close();
    }

    static class StopRouteInfo {

        private Stop stop;
        private HashSet<String> routes;

        public StopRouteInfo(Stop stop) {
            this.stop = stop;
            routes = new HashSet<>();
        }

        public Collection<String> getRoutes() {
            return routes;
        }

        public void addRouteToStop(String routeNumber) {
            routes.add(routeNumber);
        }

        public Stop getStop() {
            return stop;
        }


    }
}
