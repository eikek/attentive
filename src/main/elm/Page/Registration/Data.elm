module Page.Registration.Data exposing (..)

import Http
import Data.Flags exposing (Flags)
import Data.AccountExists exposing (AccountExists)

type alias Model =
    { flags: Flags
    , nameField: String
    , passField: String
    , invField: String
    , nameExists: Bool
    , regError: Maybe Bool
    }

emptyModel: Flags -> Model
emptyModel flags =
    { flags = flags
    , nameField = ""
    , passField = ""
    , invField = ""
    , nameExists = False
    , regError = Nothing
    }

type Msg
    = SetName String
    | SetPass String
    | SetInvite String
    | NameExistsResp (Result Http.Error AccountExists)
    | Register
    | RegisterResp (Result Http.Error ())
