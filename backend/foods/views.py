# backend/foods/views.py

from django.http import HttpResponse

def index(request):
    return HttpResponse("Foods app is working correctly!")
