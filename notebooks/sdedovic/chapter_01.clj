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

;; ## 1.6 How to Find Lagrangians
;; ### Constant acceleration

(defn L-uniform-acceleration [m g]
  (fn [local]
    (let [q (coordinate local)
          v (velocity local)
          y (ref q 1)]
      (- (* 1/2 m (square v)) (* m g y)))))

(render
  (((Lagrange-equations
      (L-uniform-acceleration 'm 'g))
    (up (literal-function 'x)
        (literal-function 'y)))
   't))

;; ### Central force field
(defn L-central-rectangular [m U]
  (fn [local]
    (let [q (coordinate local)
          v (velocity local)]
      (- (* 1/2 m (square v))
         (U (sqrt (square q)))))))

(render
  (((Lagrange-equations 
      (L-central-rectangular 'm (literal-function 'U)))
    (up (literal-function 'x)
        (literal-function 'y)))
   't))

;; ## 1.6.1 Coordinate Transformations

(defn F->C-1 
  "Given a coordinate tranformation F, this procedure returns 
    a function C that transforms the supplied local tuples."
  [F]
  (fn [local]
    (up (first local)
        (F local)
        (+ (((partial 0) F) local)
           (* (((partial 1) F) local)
              (velocity local))))))

(defn p->r-1 [local]
  (let [polar-tuple (coordinate local)
        r (ref polar-tuple 0)
        phi (ref polar-tuple 1)
        x (* r (cos phi))
        y (* r (sin phi))]
    (up x y)))

(render
  (velocity
    ((F->C-1 p->r)
     (up 
       't 
       (up 'r 'varphi) 
       (up 'rdot 'varphidot)))))

(defn L-central-polar-1 [m U]
  (compose (L-central-rectangular m U) (F->C-1 p->r-1)))

(render
  ((L-central-polar-1 'm (literal-function 'U))
   (up 
     't 
     (up 'r 'varphi) 
     (up 'rdot 'varphidot))))

;; ### Coriolis and centrifugal foces
(defn L-free-polar [m]
  (compose (L-free-particle m) (F->C p->r)))

(defn F 
  "Time-dependent transformation to rotating coordinates with rate of
    rotation Omega."
  [Omega]
  (fn [local]
    (let [t (first local)
          r (ref (coordinate local) 0)
          theta (ref (coordinate local) 1)]
      (up r (+ theta (* Omega t))))))

(defn L-rotating-polar [m Omega]
  (compose (L-free-polar m) (F->C (F Omega))))

(defn L-rotating-rectangular [m Omega]
  (compose (L-rotating-polar m Omega) (F->C r->p)))

;; The Lagrangian:
(render
  ((L-rotating-rectangular 'm 'Omega)
   (up 't
       (up 'x_r 'y_r)
       (up 'xdot_r 'ydot_r))))

;; The Lagrange Equations of motion:
(render
  (((Lagrange-equations (L-rotating-rectangular 'm 'Omega))
    (up (literal-function 'x_r) (literal-function 'y_r)))
   't))

;; ## 1.7 Evolution of Dynamic State

;; Gives the acceleration as a function of the state tuple:
(defn Lagrangian->acceleration [L]
  (let [p ((partial 2) L)
        f ((partial 1) L)]
    (solve-linear-left
      ((partial 2) p)
      (- f
         (+ ((partial 0) p)
            (* ((partial 1) p) velocity))))))

;; Compute the derivative of the state as a function of the state:
(defn Lagrangian->state-derivative-1 [L]
  (let [acceleration (Lagrangian->acceleration L)]
    (fn [state]
      (up 1
          (velocity state)
          (acceleration state)))))


;; Example, harmonic oscillator:
(defn harmonic-state-derivative [m k]
  (Lagrangian->state-derivative-1 (L-harmonic m k)))

(render
  ((harmonic-state-derivative 'm 'k)
   (up 't (up 'x 'y) (up 'v_x 'v_y))))


(defn qv->state-path [q v]
  (fn [t]
    (up t (q t) (v t))))

(defn Lagrange-equations-first-order-1 [L]
  (fn [q v]
    (let [state-path (qv->state-path q v)]
      (- (D state-path)
         (compose (Lagrangian->state-derivative-1 L)
                  state-path)))))

(render
  (((Lagrange-equations-first-order-1 (L-harmonic 'm 'k))
    (up (literal-function 'x)
        (literal-function 'y))
    (up (literal-function 'v_x)
        (literal-function 'v_y)))
   't))

;; ### Numerical Integration
(render 
  ((state-advancer harmonic-state-derivative 2.0 1.0)
   (up 1.0 (up 1.0 2.0) (up 3.0 4.0))
   10.0
   1.0e-12))

(defn periodic-drive [amplitude frequency phase]
  (fn [t]
    (* amplitude (cos (+ (* frequency t) phase)))))

(letfn [(T-pend [m l g ys]
          (fn [local]
            (let [t (first local)
                  theta (coordinate local)
                  thetadot (velocity local)
                  vys (D ys)]
              (* 1/2 m
                 (+ (square (* l thetadot))
                    (square (vys t))
                    (* 2 l (vys t) thetadot (sin theta)))))))
        (V-pend [m l g ys]
          (fn [local]
            (let [t (first local)
                  theta (coordinate local)]
              (* m g (- (ys t) (* l (cos theta)))))))]
  (def L-pend (- T-pend V-pend)))

(defn L-periodically-driven-pendulum [m l g A omega]
  (let [ys (periodic-drive A omega 0)]
    (L-pend m l g ys)))

(render
  (((Lagrange-equations (L-periodically-driven-pendulum 'm 'l 'g 'A 'omega))
    (literal-function 'theta))
   't))

(defn pend-state-derivative [m l g A omega]
  (Lagrangian->state-derivative
    (L-periodically-driven-pendulum m l g A omega)))

(render
  ((pend-state-derivative 'm 'l 'g 'A 'omega)
   (up 't 'theta 'thetadot)))

(let [values (integrate-state-derivative
               pend-state-derivative
               [1.0                 ;m          = 1.0kg
                1.0                 ;l          = 1m
                9.8                 ;g          = 9.8m/s^2
                0.1                 ;a          = 1/10 m
                (* 2.0 (sqrt 9.8))] ;omega
               (up 
                 0.0                ;t_0        = 0 s
                 1.0                ;theta_0    = 1radian
                 1e0)               ;thetadot_0 = 0.0 radians/s
               100.0                ;final time
               0.01)                ; step between
      ys (->> values 
              (map second)
              (map #(- (mod (+ Math/PI %) Math/TAU) Math/PI)))]
  (clerk/plotly {:data [{:x (map first values)
                         :y ys}]
                 :layout {:yaxis {:range [(- Math/PI) Math/PI]}}
                 :config {:staticPlot true
                         :displayLogo false}}))
