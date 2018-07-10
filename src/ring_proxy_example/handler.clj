(ns ring-proxy-example.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring-proxy-example.proxy :refer [wrap-proxy]]))
(def app
  (-> routes
      (wrap-proxy "/" "http://localhost:8765")
  ))
