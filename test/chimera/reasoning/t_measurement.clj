(ns chimera.reasoning.t-measurement
    (:use midje.sweet)
    (:require [quux.core :as core])

    )
(facts "about `split`"
       (str/split "a/b/c" #"/") => ["a" "b" "c"]
       (str/split "" #"irrelvant") => [""]
       (str/split "no regexp matches" #"a+\s+[ab]") => ["no regexp matches"])

(facts "about `first-element`"
       (fact "it normally returns the first element"
             (first-element [1 2 3] :default) => 1
             (first-element '(1 2 3) :default) => 1)

       ;; I'm a little unsure how Clojure types map onto the Lisp I'm used to.
       (fact "default value is returned for empty sequences"
             (first-element [] :default) => :default
             (first-element '() :default) => :default
             (first-element nil :default) => :default
             (first-element (filter even? [1 3 5]) :default) => :default))