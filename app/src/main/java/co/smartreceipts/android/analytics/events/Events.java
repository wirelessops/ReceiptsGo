package co.smartreceipts.android.analytics.events;

public final class Events {

    private enum Category implements Event.Category {
        Purchases, Navigation, Reports, Receipts, Distance, Generate, Ratings, Informational, Sync, Ocr, Identity, Intent, Permissions
    }

    public static final class Purchases {
        public static final Event PurchaseSuccess = new DefaultEvent(Category.Purchases, "PurchaseSuccess");
        public static final Event PurchaseFailed = new DefaultEvent(Category.Purchases, "PurchaseFailed");
        public static final Event ShowPurchaseIntent = new DefaultEvent(Category.Purchases, "ShowPurchaseIntent");
        public static final Event AdUpsellShown = new DefaultEvent(Category.Purchases, "AdUpsellShown");
        public static final Event AdUpsellShownOnFailure = new DefaultEvent(Category.Purchases, "AdUpsellShownOnFailure");
        public static final Event AdUpsellTapped = new DefaultEvent(Category.Purchases, "AdUpsellTapped");
    }

    public static final class Navigation {
        public static final Event SettingsOverflow = new DefaultEvent(Category.Navigation, "SettingsOverflow");
        public static final Event BackupOverflow = new DefaultEvent(Category.Navigation, "BackupOverflow");
        public static final Event OcrConfiguration = new DefaultEvent(Category.Navigation, "OcrConfiguration");
        public static final Event SmartReceiptsPlusOverflow = new DefaultEvent(Category.Navigation, "SmartReceiptsPlusOverflow");
    }

    public static final class Reports {
        public static final Event PersistNewReport = new DefaultEvent(Category.Reports, "PersistNewReport");
        public static final Event PersistUpdateReport = new DefaultEvent(Category.Reports, "PersistUpdateReport");
    }

    public static final class Receipts {
        public static final Event AddPictureReceipt = new DefaultEvent(Category.Receipts, "AddPictureReceipt");
        public static final Event AddTextReceipt = new DefaultEvent(Category.Receipts, "AddTextReceipt");
        public static final Event ImportPictureReceipt = new DefaultEvent(Category.Receipts, "ImportPictureReceipt");
        public static final Event ReceiptMenuEdit = new DefaultEvent(Category.Receipts, "ReceiptMenuEdit");
        public static final Event ReceiptMenuRetakePhoto = new DefaultEvent(Category.Receipts, "ReceiptMenuRetakePhoto");
        public static final Event ReceiptMenuViewPdf = new DefaultEvent(Category.Receipts, "ReceiptMenuViewPdf");
        public static final Event ReceiptMenuViewImage = new DefaultEvent(Category.Receipts, "ReceiptMenuViewImage");
        public static final Event ReceiptMenuDelete = new DefaultEvent(Category.Receipts, "ReceiptMenuDelete");
        public static final Event ReceiptMenuMoveCopy = new DefaultEvent(Category.Receipts, "ReceiptMenuMoveCopy");
        public static final Event ReceiptMenuSwapUp = new DefaultEvent(Category.Receipts, "ReceiptMenuSwapUp");
        public static final Event ReceiptMenuSwapDown = new DefaultEvent(Category.Receipts, "ReceiptMenuSwapDown");

        public static final Event ReceiptImageViewRotateCcw = new DefaultEvent(Category.Receipts, "ReceiptImageViewRotateCcw");
        public static final Event ReceiptImageViewRotateCw = new DefaultEvent(Category.Receipts, "ReceiptImageViewRotateCw");
        public static final Event ReceiptImageViewRetakePhoto = new DefaultEvent(Category.Receipts, "ReceiptImageViewRetakePhoto");

        public static final Event PersistNewReceipt = new DefaultEvent(Category.Receipts, "PersistNewReceipt");
        public static final Event PersistUpdateReceipt = new DefaultEvent(Category.Receipts, "PersistUpdateReceipt");
        public static final Event RequestExchangeRate = new DefaultEvent(Category.Receipts, "RequestExchangeRate");
        public static final Event RequestExchangeRateSuccess = new DefaultEvent(Category.Receipts, "RequestExchangeRateSuccess");
        public static final Event RequestExchangeRateFailed = new DefaultEvent(Category.Receipts, "RequestExchangeRateFailed");
        public static final Event RequestExchangeRateFailedWithNull = new DefaultEvent(Category.Receipts, "RequestExchangeRateFailedWithNull");
    }

    public static final class Distance {
        public static final Event PersistNewDistance = new DefaultEvent(Category.Distance, "PersistNewDistance");
        public static final Event PersistUpdateDistance = new DefaultEvent(Category.Distance, "PersistUpdateDistance");
    }

    public static final class Generate {
        public static final Event GenerateReports = new DefaultEvent(Category.Generate, "GenerateReports");
        public static final Event FullPdfReport = new DefaultEvent(Category.Generate, "FullPdfReport");
        public static final Event ImagesPdfReport = new DefaultEvent(Category.Generate, "ImagesPdfReport");
        public static final Event CsvReport = new DefaultEvent(Category.Generate, "CsvReport");
        public static final Event StampedZipReport = new DefaultEvent(Category.Generate, "StampedZipReport");
        public static final Event ReportPdfRenderingError = new DefaultEvent(Category.Generate, "ReportPdfRenderingError");
    }

    public static final class Ratings {
        public static final Event RatingPromptShown = new DefaultEvent(Category.Ratings, "RatingPromptShown");
        public static final Event UserAcceptedRatingPrompt = new DefaultEvent(Category.Ratings, "UserAcceptedRatingPrompt");
        public static final Event UserDeclinedRatingPrompt = new DefaultEvent(Category.Ratings, "UserDeclinedRatingPrompt");
        public static final Event UserAcceptedSendingFeedback = new DefaultEvent(Category.Ratings, "UserAcceptedSendingFeedback");
        public static final Event UserDeclinedSendingFeedback = new DefaultEvent(Category.Ratings, "UserDeclinedSendingFeedback");
        public static final Event UserSelectedRate = new DefaultEvent(Category.Ratings, "UserSelectedRate");
        public static final Event UserSelectedNever = new DefaultEvent(Category.Ratings, "UserSelectedNever");
        public static final Event UserSelectedLater = new DefaultEvent(Category.Ratings, "UserSelectedLater");
    }

    public static final class Informational {
        public static final Event ConfigureReport = new DefaultEvent(Category.Informational, "ConfigureReport");
        public static final Event ClickedGenerateReportTip = new DefaultEvent(Category.Informational, "ClickedGenerateReportTip");
    }

    public static final class Sync {
        public static final Event DisplaySyncError = new DefaultEvent(Category.Sync, "DisplaySyncError");
        public static final Event ClickSyncError = new DefaultEvent(Category.Sync, "ClickSyncError");
        public static final Event DriveCompletionEventHandledWithSuccess = new DefaultEvent(Category.Sync, "DriveCompletionEventHandledWithSuccess");
        public static final Event DriveCompletionEventHandledWithFailure = new DefaultEvent(Category.Sync, "DriveCompletionEventHandledWithFailure");
        public static final Event DriveCompletionEventNotHandled = new DefaultEvent(Category.Sync, "DriveCompletionEventNotHandled");
    }

    public static final class Identity {
        public static final Event UserLogin = new DefaultEvent(Category.Identity, "UserLogin");
        public static final Event UserLoginSuccess = new DefaultEvent(Category.Identity, "UserLoginSuccess");
        public static final Event UserLoginFailure = new DefaultEvent(Category.Identity, "UserLoginFailure");
        public static final Event UserSignUp = new DefaultEvent(Category.Identity, "UserSignUp");
        public static final Event UserSignUpSuccess = new DefaultEvent(Category.Identity, "UserSignUpSuccess");
        public static final Event UserSignUpFailure = new DefaultEvent(Category.Identity, "UserSignUpFailure");
        public static final Event UserLogout = new DefaultEvent(Category.Identity, "UserLogout");
        public static final Event UserLogoutSuccess = new DefaultEvent(Category.Identity, "UserLogoutSuccess");
        public static final Event UserLogoutFailure = new DefaultEvent(Category.Identity, "UserLogoutFailure");
        public static final Event PushTokenUploadRequired = new DefaultEvent(Category.Identity, "PushTokenUploadRequired");
        public static final Event PushTokenSucceeded = new DefaultEvent(Category.Identity, "PushTokenSucceeded");
        public static final Event PushTokenFailed = new DefaultEvent(Category.Identity, "PushTokenFailed");
    }

    public static final class Ocr {
        public static final Event OcrInfoTooltipShown = new DefaultEvent(Category.Ocr, "OcrInfoTooltipShown");
        public static final Event OcrInfoTooltipOpen = new DefaultEvent(Category.Ocr, "OcrInfoTooltipOpen");
        public static final Event OcrInfoTooltipDismiss = new DefaultEvent(Category.Ocr, "OcrInfoTooltipDismiss");
        public static final Event OcrViewConfigurationPage = new DefaultEvent(Category.Ocr, "OcrViewConfigurationPage");
        public static final Event OcrPurchaseClicked = new DefaultEvent(Category.Ocr, "OcrPurchaseClicked");
        public static final Event OcrIsEnabledToggled = new DefaultEvent(Category.Ocr, "OcrIsEnabledToggled");
        public static final Event OcrIncognitoModeToggled = new DefaultEvent(Category.Ocr, "OcrIncognitoModeToggled");
        public static final Event OcrRequestStarted = new DefaultEvent(Category.Ocr, "OcrRequestStarted");
        public static final Event OcrPushMessageReceived = new DefaultEvent(Category.Ocr, "OcrPushMessageReceived");
        public static final Event OcrPushMessageTimeOut = new DefaultEvent(Category.Ocr, "OcrPushMessageTimeOut");
        public static final Event OcrRequestSucceeded = new DefaultEvent(Category.Ocr, "OcrRequestSucceeded");
        public static final Event OcrRequestFailed = new DefaultEvent(Category.Ocr, "OcrRequestFailed");
    }

    public static final class Intents {
        public static final Event ReceivedActionableIntent = new DefaultEvent(Category.Intent, "ReceivedActionableIntent");
    }

    public static final class Permissions {
        public static final Event PermissionRequested = new DefaultEvent(Category.Permissions, "PermissionRequested");
        public static final Event PermissionGranted = new DefaultEvent(Category.Permissions, "PermissionGranted");
        public static final Event PermissionDenied = new DefaultEvent(Category.Permissions, "PermissionDenied");
    }


}
