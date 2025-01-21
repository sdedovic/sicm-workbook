;; # SICM Workbook

^{:nextjournal.clerk/toc true}
(ns sdedovic.chapter-01
  (:refer-clojure
    :exclude [+ - * / zero? compare divide numerator denominator
              infinite? abs ref partial =])
  (:require [emmy.env :as e :refer :all]
            [emmy.polynomial :refer [evaluate]]
            [nextjournal.clerk :as clerk]))


;; ## Helper Functions
(def render (comp clerk/tex ->TeX simplify))


;; ----
;; # Chapter 1 - Lagrangian Mechanics
;; ## 1.4 Computing Actions
(defn L-free-particle [mass]
  (fn [local]
    (let [v (velocity local)]
      (* 1/2 mass (dot-product v v)))))

(def q
  (up (literal-function 'x)
      (literal-function 'y)
      (literal-function 'z)))

(render 
  (q 't))

(render 
  ((D q) 't))

(render
  ((Gamma q) 't))

(render
  (simplify
    ((compose (L-free-particle 'm) (Gamma q)) 't)))

(defn Lagrangian-action-1 [L q t1 t2]
  (definite-integral (compose L (Gamma q)) t1 t2))

(defn test-path [t]
  (up (+ (* 4 t) 7)
      (+ (* 3 t) 5)
      (+ (* 2 t) 1)))

(Lagrangian-action-1 (L-free-particle 3) test-path 0 10)

;; ### Paths of minimum action

(defn make-eta [nu t1 t2]
  (fn [t] 
    (* (- t t1) (- t t2) (nu t))))

(defn varied-free-particle-action [mass q nu t1 t2]
  (fn [eps]
    (let [eta (make-eta nu t1 t2)]
      (Lagrangian-action-1 
        (L-free-particle mass)
        (+ q (* eps eta))
        t1 t2))))

;; same as previous action calculation, but we vary the path a bit, showing
;;  that is has to be larger than the straight path
((varied-free-particle-action 3 test-path
                              (up sin cos square)
                              0 10)
 0.001)

(minimize
  (varied-free-particle-action 3 test-path
                               (up sin cos square)
                               0 10)
  -2 1)

;; ### Finding trajectories that minimize the action
(defn make-path [t0 q0 t1 q1 qs]
  (let [n (count qs)
        ts (linear-interpolants t0 t1 n)]
    (Lagrange-interpolation-function 
      (concat [q0] qs [q1])
      (concat [t0] ts [t1]))))

(defn parametric-path-action [Lagrangian t0 q0 t1 q1]
  (fn [qs]
    (let [path (make-path t0 q0 t1 q1 qs)]
      (Lagrangian-action-1 Lagrangian path t0 t1))))

(defn find-path-1 [Lagrangian t0 q0 t1 q1 n]
  (let [initial-qs (linear-interpolants q0 q1 n)
        minimizing-qs (multidimensional-minimize
                        (parametric-path-action Lagrangian t0 q0 t1 q1)
                        initial-qs)]
        (make-path t0 q0 t1 q1 minimizing-qs)))

(defn L-harmonic [m k]
  (fn [local]
    (let [q (coordinate local)
          v (velocity local)]
      (- (* 1/2 m (square v)) (* 1/2 k (square q))))))

(def q2
  (find-path-1 (L-harmonic 1 1) 0 1 (/ Math/PI 2) 0 5))

(let [xs (linear-interpolants 0 (/ Math/PI 2) 25)
      ys (map #(evaluate q2 [%]) xs)]
  (clerk/plotly {:data [{:x xs :y ys}]}))


;; ## 1.5.2 Computing Lagrange's Equations

(defn Lagrange-equations-1 [Lagrangian]
  (fn [q]
    (- (D (compose ((partial 2) Lagrangian) (Gamma q)))
       (compose ((partial 1) Lagrangian) (Gamma q)))))

(defn test-path-2 [t]
  (up (+ (* 'a t) 'a0)
      (+ (* 'b t) 'b0)
      (+ (* 'c t) 'c0)))

(render
  (((Lagrange-equations-1 (L-free-particle 'm))
    test-path)
   't))

(render
  (((Lagrange-equations-1 (L-free-particle 'm))
    (literal-function 'x))
   't))

;; ### The harmonic Oscillator

(defn proposed-solution [t]
  (* 'A (cos (+ (* 'omega t) 'phi))))

(render
  (((Lagrange-equations-1 (L-harmonic 'm 'k))
    proposed-solution)
   't))

;; ### Exercise 1.11: Kepler's third law

;; Lagrangian for "central force" in polar coordinates. This is rotation
;;  kinetic energy minus some potential $V$ that depends on the distance $r$ between the two particles.
(defn L-central-polar [m V]
  (fn [local]
    (let [q (coordinate local)
          qdot (velocity local)
          r (ref q 0)
          phi (ref qdot 1)
          rdot (ref qdot 0)
          phidot (ref qdot 1)]
      (- (* 1/2 m
            (+ (square rdot) (square (* r phidot))))
         (V r)))))

(defn gravitational-energy [G m1 m2]
  (fn [r]
    (- (/ (* G m1 m2) r))))
