module Data.Flags exposing (..)

import Data.Account exposing (Account)
import Data.RegistrationMode exposing (RegistrationMode(..))


type alias Config =
    { appName: String
    , base: String
    , apiBase: String
    , registrationMode: String
    }

type alias Flags =
    { account: Maybe Account
    , config: Config
    }

regMode: Flags -> RegistrationMode
regMode flags =
    case (String.toLower flags.config.registrationMode) of
        "open" -> Open
        "invite" -> Invite
        _ -> Closed
