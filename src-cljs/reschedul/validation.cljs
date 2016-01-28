(ns reschedul.validation
  (:require
    [bouncer.core :as b]
    [bouncer.validators :as v]))


(defn validate-user-creation [user]
  (b/validate
    user
    [:username] v/required
    [:password] v/required
    [:full-name] v/required
    [:role] v/required
    [:email] v/required))



(defn validate-proposal-creation [proposal]
  (b/validate
    proposal
    [:primary-contact-name] [v/required [v/matches #"^\d+$"]]
    [:proposer-username] v/required
    [:primary-contact-email] v/required
    [:primary-contact-phone] [v/required    [v/matches #"^\d+$"]]
    [:primary-contact-method] v/required
    [:primary-contact-zipcode] [v/required    [v/matches #"^\d+$"]]
    [:primary-contact-role] v/required
    [:title] v/required
    [:category] v/required))




