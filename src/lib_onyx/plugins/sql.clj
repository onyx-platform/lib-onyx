(ns lib-onyx.plugins.sql
  (:require [lib-onyx.job.utils :refer [add-to-job instrument-plugin-lifecycles
                                        find-task-by-key update-task catalog-entrys-by-name]]
            [traversy.lens :refer :all :rename {update lupdate}]))

(defn add-sql-input-lens
  ([job task] (add-sql-input job task nil))
  ([job task opts]
   (if-let [entry (-> job (view-single (catalog-entrys-by-name task)))]
     (-> job
         (update-in [:lifecycles] conj {:lifecycle/task task
                                        :lifecycle/calls :onyx.plugin.sql/read-rows-calls})
         (lupdate (catalog-entrys-by-name task) (partial merge opts))))))

(defn add-sql-output-lens
  ([job task] (add-sql-output job task nil))
  ([job task opts]
   (if-let [entry (-> job (view-single (catalog-entrys-by-name task)))]
     (-> job
         (update-in [:lifecycles] conj {:lifecycle/task task
                                        :lifecycle/calls :onyx.plugin.sql/write-rows-calls})
         (lupdate (catalog-entrys-by-name task) (partial merge opts))))))

(defn add-sql-input
  ([job task] (add-sql-input job task nil))
  ([job task opts]
   (if-let [entry (first (filter #(= (:onyx/name %) task) (:catalog job)))]
     (-> job
         (update-in [:lifecycles] conj {:lifecycle/task task
                                        :lifecycle/calls :onyx.plugin.sql/read-rows-calls})
         (update-in [:catalog] (fn [catalog]
                                 (replace {entry (merge opts entry)} catalog)))))))

(defn add-sql-output
  ([job task] (add-sql-output job task nil))
  ([job task opts]
   (if-let [entry (first (filter #(= (:onyx/name %) task) (:catalog job)))]
     (-> job
         (update-in [:lifecycles] conj {:lifecycle/task task
                                        :lifecycle/calls :onyx.plugin.sql/write-rows-calls})
         (update-in [:catalog] (fn [catalog]
                                 (replace {entry (merge opts entry)} catalog)))))))
