;;meant to be loaded from the marathon repl and contains inputs that
;;will be frequently toggled for doing taa runs.

;;To use the latest version of docjure with MARATHON, you need to put
;;the dependency [dk.ative/docjure "1.16.0"] in the m4 project.clj.
;;Used docjure proper instead of spork.util.excel.docjure because
;;the latest version returns nil for blank cells.  blank cells were
;;filtered out in older versions.  This allows us to copy  the data as
;;is from an xlsx worksheet and then copy the same data to a tab
;;delimited text file, similar to Excel->Save As->tab delimited text
;;file in order to save FORGE SRC by day for demand builder.
(ns taa.core)
;;need to change these manually right now for the cannibal and idaho
;;records. This is okay, highlights the fact that there is a separate
;;pipeline for these just in case we want to go back to assuming
;;cannibalized units can fill idaho.  For the most recent TAA, we
;;are currently assuming that cannibalized units can't be used for anything!
(def conflict-timeline {:Duration 969
                        :StartDay 731})
(load-file "/home/craig/workspace/taa/src/taa/core.clj")

(def input-map {:resources-root "/home/craig/workspace/taa/resources/"
                ;;;;;;what usage.clj should be specifying:
                ;;path to SupplyDemand (also has a policy_map worksheet)
                ;; no matching clause exceptions might be for N/As in excel formulas
                ;; when reading workbooks into clojure. Change these values to
                ;; something else, like "na".
                ;;will also get an error when some values for idaho and RC availble
                ;;aren't numbers..... turn stuff to 0s or delete.
                ;;make sure strength in SRC_By_Day has numbers! if
                ;;not, set to 0)
                ;;Assume SRC_By_Day is in SupplyDemand
                ;;The name of the supply demand file that exists in
                ;;the resources-root folder
                :supp-demand-name "SupplyDemand_Colorado.xlsx"
                ;;a set of vignettes to keep from SupplyDemand (ensure SupplyDemand
                ;;has RCAvailable and Idaho)
                :vignettes #{"AlaskaFwd"
                             "AlaskaRot"
                             "Maine1"
                             "Maine2"
                             "Maine3"
                                        ;"Colorado"
                             "Wyoming"
                                        ;"Idaho"
                             "Vermont"}
                ;; a default RC policy name
                :default-rc-policy "TAA_2125_Capacity_RC_1"
                ;;a post-process function with priority, category, and sourcefirst
                ;;post process demand records to copy vignette to DemandGroup and
                ;;set priorities
                ;;Change Forward stationed category to NonBOG , SourceFirst to NOT-RC-MIN
                ;;PTDOS are regular category =Rotational and SourceFirst = NOT-RC
                ;;Idaho-Competition is category = NonBOG and SourceFirst = NOT-AC
                ;;Idaho is category = NonBOG and SourceFirst = NOT-AC-MIN 
                :set-demand-params
                (fn [{:keys [Vignette Category SourceFirst] :as r}]
                  (assoc r :DemandGroup Vignette
                         :Priority (case Vignette
                                     "RC_NonBOG-War" 1
                                     "AlaskaFwd" 1
                                     "AlaskaRot" 6
                                     "Maine1" 2
                                     "Maine2" 2
                                     "Maine3" 2
                                     "SE-Colorado" 4
                                     "Wyoming" 5
                                     "Idaho" 3
                                     "Vermont" 6)
                         :Category (if (clojure.string/includes? Vignette
                                                                 "Fwd")
                                     "NonBOG"
                                     (case Vignette
                                       "Maine1" "NonBOG"
                                       "Maine2" "NonBOG"
                                       ;;"Maine3" "Rotational"
                                       Category
                                       ))
                         :SourceFirst (if (clojure.string/includes? Vignette
                                                                    "Fwd")
                                        "NOT-RC-MIN"
                                        (case Vignette
                                          "Maine1"
                                          "NOT-AC"
                                          "Maine2" "NOT-AC"
                                          SourceFirst))))
                ;;an excursion name to prepend to output files
                :identifier "Colorado"
                ;;the name of the the timeline file that exists in resources-path
                :timeline-name "timeline_Colorado.xlsx"
                ;;the name of the base marathon file that exists in
                ;;the resources-path
                :base-m4-name "base-testdata-v7.xlsx"
                ;;phases for results.txt
                :phases [["comp" 1 821]
                         ["phase-1" 822 854]
                         ["phase-2" 855 974]
                         ["phase-3" 975 1022]
                         ["phase-4" 1023 1789]]
                ;;compo-lengths for rand-runs               
                :compo-lengths {"AC" 1 "RC" 2 "NG" 3}
                ;;usually 30
                :reps 1
                ;;low bound on AC reductions, usually 0 (no AC inventory)
                :lower 1
                ;;Max ac growth.  usually 1 (base taa inventory)
                :upper 1
                ;;20 for taa runs on our 2 fast computers
                :threads 1
                })

(do-taa input-map)
;;if you want preprocess first and then visually check, call
;;preprocess-taa first and then call do-taa-runs on the output workbook.
