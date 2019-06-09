module Page.Login.Data exposing (..)

import Http
import Data.Flags exposing (Flags)
import Data.LoginResult exposing (LoginResult)

type alias Model =
    { flags: Flags
    , username: String
    , password: String
    , result: Maybe LoginResult
    }

empty: Flags -> Model
empty flags =
    { flags = flags
    , username = ""
    , password = ""
    , result = Nothing
    }

type Msg
    = SetUsername String
    | SetPassword String
    | Authenticate
    | AuthResp (Result Http.Error LoginResult)
